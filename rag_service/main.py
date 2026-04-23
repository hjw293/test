# rag_service/main.py
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import chromadb
from chromadb.config import Settings
import ollama
import requests

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
chroma_client = chromadb.Client(Settings(
    persist_directory="./chroma_db",
    anonymized_telemetry=False
))

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

你可以调用以下函数来获取实时数据：
- get_device_status: 获取设备状态统计（当用户询问设备数量、停机状态时必须调用）

回答要求：
1. 当用户询问设备数量、停机数量时，必须先调用 get_device_status 函数获取准确数据
2. 直接使用函数返回的数据回答，不要估算
3. 如果知识库有相关信息，可以结合回答
4. 回答要简洁专业"""

@app.post("/api/rag/chat", response_model=AnswerResponse)
async def chat_with_function(request: ChatRequest):
    try:
        # 1. 检索知识库
        contexts = retrieve_context(request.question)

        # 2. 检查是否需要获取设备状态
        question = request.question.lower()
        need_device_status = any(k in question for k in ['设备', '停机', '机器', '台数', '多少台'])

        device_info = ""
        if need_device_status:
            # 先获取实时数据
            device_info = call_function("get_device_status", {})

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
