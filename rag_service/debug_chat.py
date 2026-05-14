# rag_service/debug_chat.py
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import chromadb
import ollama
import requests
import re

app = FastAPI(title="RAG Knowledge Base API - Debug")

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

class ChatRequest(BaseModel):
    question: str
    chat_history: Optional[List[Dict[str, str]]] = []

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
            # Try API call
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