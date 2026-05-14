# rag_service/main.py
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import chromadb
import ollama
import requests
import re

app = FastAPI(title="RAG Knowledge Base API with Function Calling")

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化 ChromaDB
chroma_client = chromadb.PersistentClient(path="./chroma_db")

def get_collection():
    try:
        return chroma_client.get_collection("sensor_knowledge")
    except:
        return chroma_client.create_collection(
            name="sensor_knowledge",
            metadata={"description": "染整监控系统知识库"}
        )

collection = get_collection()

# ============ 函数调用配置 ============

def call_function(function_name: str, arguments: dict) -> str:
    """执行函数调用"""
    if function_name == "get_device_status":
        try:
            # 调用后端 API 获取设备状态
            response = requests.get("http://localhost:8080/api/alarm/device-status-count")
            if response.ok:
                data = response.json()
                if data.get("code") == 200:
                    device_data = data.get("data", {})
                    stopped = device_data.get("stopped", 0)
                    total = device_data.get("total", 0)
                    return f"当前系统共有 {total} 台设备，其中 {stopped} 台处于停机状态。"
            return "无法获取设备状态数据"
        except Exception as e:
            return f"获取设备状态失败: {str(e)}"
    elif function_name == "count_report_configs":
        try:
            # 调用后端 API 统计报表数量
            response = requests.get("http://localhost:8080/api/batch-report/count")
            if response.ok:
                data = response.json()
                if data.get("code") == 200:
                    report_data = data.get("data", {})
                    total = report_data.get("total", 0)
                    energy = report_data.get("energyCount", 0)
                    health = report_data.get("healthCount", 0)
                    return f"报表配置统计：共 {total} 个报表，其中能耗报告 {energy} 个，健康指数报告 {health} 个。"
            return "无法获取报表统计数据"
        except Exception as e:
            return f"获取报表统计失败: {str(e)}"
    elif function_name == "count_response_req":
        try:
            # 调用后端 API 获取报警性质种类数量
            response = requests.get("http://localhost:8080/api/alarm/response-req-count")
            if response.ok:
                data = response.json()
                if data.get("code") == 200:
                    alarm_data = data.get("data", {})
                    count = alarm_data.get("count", 0)
                    types = alarm_data.get("types", [])
                    types_str = "、".join(types) if types else "无"
                    return f"报警配置系统共有 {count} 种报警性质，分别是：{types_str}。"
            return "无法获取报警性质数据"
        except Exception as e:
            return f"获取报警性质失败: {str(e)}"
    elif function_name == "get_response_req_stats":
        try:
            # 调用后端 API 获取每种报警性质的数量
            response = requests.get("http://localhost:8080/api/alarm/response-req-stats")
            if response.ok:
                data = response.json()
                if data.get("code") == 200:
                    stats = data.get("data", [])
                    if not stats:
                        return "暂无报警性质统计数据"
                    stats_str = "、".join([f"{item.get('type', '未知')} {item.get('count', 0)} 条" for item in stats])
                    return f"各报警性质数量统计：{stats_str}。"
            return "无法获取报警性质统计"
        except Exception as e:
            return f"获取报警性质统计失败: {str(e)}"
    return f"未知函数: {function_name}"

# ==========================================

class ChatRequest(BaseModel):
    question: str
    chat_history: Optional[List[Dict[str, str]]] = []

class AnswerResponse(BaseModel):
    answer: str
    sources: List[str] = []
    function_called: Optional[str] = None

# 检索知识库
def retrieve_context(query: str, top_k: int = 3) -> List[str]:
    try:
        embed_response = ollama.embeddings(
            model="nomic-embed-text",
            prompt=query
        )
        query_embedding = embed_response["embedding"]

        results = collection.query(
            query_embeddings=[query_embedding],
            n_results=top_k
        )

        contexts = []
        if results and results.get('documents') and len(results['documents']) > 0:
            docs = results['documents'][0] if results['documents'] else []
            for doc in docs:
                if isinstance(doc, str):
                    contexts.append(doc)
                elif isinstance(doc, list):
                    contexts.extend([d for d in doc if isinstance(d, str)])
        return contexts
    except Exception as e:
        print(f"检索知识库出错: {e}")
        return []

def build_system_prompt(contexts: List[str]) -> str:
    context_text = "\n".join([f"- {ctx}" for ctx in contexts]) if contexts else "（知识库中暂无相关内容）"

    return f"""你是一个染整智能监控系统的智能客服助手。

知识库内容：
{context_text}

回答要求：
1. 如果用户询问设备数量、停机数量等问题，直接根据提供的数据回答，不要提及数据来源或调用方式
2. 如果知识库中有相关信息，直接使用并回答
3. 如果知识库中没有相关内容或不知道如何回答，请直接说"抱歉，我目前没有这方面的信息，无法回答您的问题"
4. 回答要简洁专业，通俗易懂
5. 绝对不要向用户提及任何代码、函数、方法等技术人员相关内容"""

@app.post("/api/rag/chat", response_model=AnswerResponse)
async def chat_with_function(request: ChatRequest):
    try:
        # 1. 检索知识库
        contexts = retrieve_context(request.question)

        # 2. 检查是否需要获取设备状态
        question = request.question.lower()
        need_device_status = any(k in question for k in ['设备', '停机', '机器', '台数', '多少台'])

        # 3. 检查是否需要获取报表统计
        need_report_count = any(k in question for k in ['报表', '报告', '能耗', '健康', '多少个', '数量', '多少种'])

        # 4. 检查是否需要获取报警性质
        need_response_req = any(k in question for k in ['报警性质', '报警类型', '报警有哪些', '有几种报警'])
        need_response_req_stats = any(k in question for k in ['报警性质数量', '报警性质统计', '每种报警', '各报警', '多少条', '多少个报警'])

        device_info = ""
        report_info = ""
        response_req_info = ""
        response_req_stats_info = ""

        if need_device_status:
            # 先获取实时数据
            device_info = call_function("get_device_status", {})

        if need_report_count:
            # 获取报表统计数据
            report_info = call_function("count_report_configs", {})

        if need_response_req:
            # 获取报警性质统计
            response_req_info = call_function("count_response_req", {})

        if need_response_req_stats:
            # 获取每种报警性质的数量
            response_req_stats_info = call_function("get_response_req_stats", {})

        # 3. 构建系统提示
        system_prompt = build_system_prompt(contexts)

        # 4. 构建消息历史
        messages = [{"role": "system", "content": system_prompt}]

        # 添加对话历史
        if request.chat_history:
            for msg in request.chat_history[-5:]:  # 只保留最近5条
                messages.append({"role": msg.get("role", "user"), "content": msg.get("content", "")})

        # 如果需要设备数据，将其注入
        if need_device_status and device_info:
            messages.append({"role": "system", "content": f"\n\n重要实时数据：{device_info}"})

        # 如果需要报表统计数据，将其注入
        if need_report_count and report_info:
            messages.append({"role": "system", "content": f"\n\n重要实时数据：{report_info}"})

        # 如果需要报警性质数据，将其注入
        if need_response_req and response_req_info:
            messages.append({"role": "system", "content": f"\n\n重要实时数据：{response_req_info}"})

        # 如果需要每种报警性质数量数据，将其注入
        if need_response_req_stats and response_req_stats_info:
            messages.append({"role": "system", "content": f"\n\n重要实时数据：{response_req_stats_info}"})

        messages.append({"role": "user", "content": request.question})

        # 5. 调用 Ollama
        response = ollama.chat(
            model="qwen2.5:7b",
            messages=messages,
            options={"temperature": 0.7}
        )

        assistant_message = response['message']
        answer = assistant_message.get('content', '')

        return AnswerResponse(
            answer=answer,
            sources=contexts[:3],
            function_called="get_device_status" if need_device_status else None
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/rag/debug")
async def debug_chat(request: ChatRequest):
    question = request.question
    print(f"\n=== DEBUG ===")
    print(f"Original question: {question}")

    # Check knowledge base
    contexts = retrieve_context(question)
    print(f"Contexts from KB: {contexts}")

    # Check regex
    q_lower = question.lower()
    print(f"Lower question: {q_lower}")

    match = re.search(r'其中(.+?)有多少', q_lower)
    if match:
        specific = match.group(1).strip()
        print(f"Extracted from regex: '{specific}'")

        excluded = ['设备', '报表', '报告', '能耗', '健康']
        if any(k in specific for k in excluded):
            print("Would be excluded due to keyword filter")
        else:
            print(f"Would call API for: '{specific}'")
            try:
                resp = requests.get("http://localhost:8080/api/alarm/response-req-count-by-type",
                                   params={"responseReq": specific}, timeout=10)
                print(f"API response status: {resp.status_code}")
                print(f"API response: {resp.text}")
            except Exception as e:
                print(f"API call failed: {e}")
    else:
        print("Regex didn't match")

    print(f"=== END DEBUG ===\n")
    return {"question": question, "contexts": contexts}

@app.post("/api/rag/ingest")
async def ingest_document(text: str, metadata: dict = None):
    try:
        embed_response = ollama.embeddings(
            model="nomic-embed-text",
            prompt=text
        )

        collection.add(
            embeddings=[embed_response["embedding"]],
            documents=[text],
            metadatas=[metadata or {}],
            ids=[f"doc_{hash(text)}"]
        )

        return {"message": "文档已添加到知识库", "text": text[:100]}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/rag/search")
async def search_knowledge(query: str, top_k: int = 5):
    try:
        contexts = retrieve_context(query, top_k)
        return {"results": contexts}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
