# rag_service/init_knowledge.py
import ollama
import chromadb
from chromadb.config import Settings

# 初始化 ChromaDB
chroma_client = chromadb.Client(Settings(
    persist_directory="./chroma_db",
    anonymized_telemetry=False
))

# 创建集合
collection = chroma_client.create_collection(
    name="sensor_knowledge",
    metadata={"description": "染整监控系统知识库"}
)

# 知识库内容
knowledge_base = [
    {
        "text": "传感器数据监控平台用于实时展示染整设备的传感器数据，支持多设备管理和数据可视化。用户可以查看设备数据曲线、对比分析和历史记录。",
        "metadata": {"category": "功能介绍", "source": "系统文档"}
    },
    {
        "text": "报警配置用于设置设备报警规则。当设备状态异常（如停机）时，系统会自动触发报警通知。报警规则包括报警ID、报警内容、处理方式等配置。",
        "metadata": {"category": "报警功能", "source": "系统文档"}
    },
    {
        "text": "报表配置用于管理批量报表生成。用户可以配置报表类型、名称ID、报告类别等参数，支持灵活的报表定制。",
        "metadata": {"category": "报表功能", "source": "系统文档"}
    },
    {
        "text": "曲线组功能用于分组管理和展示设备传感器曲线。用户可以创建多个曲线组，每个组包含多个设备的传感器数据进行对比分析。",
        "metadata": {"category": "曲线功能", "source": "系统文档"}
    },
    {
        "text": "设备停机状态说明：当设备报警配置的处理方式为'停机'时，表示该设备当前处于停机状态，需要及时处理。停机可能原因包括：设备故障、维护保养、生产计划等。",
        "metadata": {"category": "设备状态", "source": "系统文档"}
    },
    {
        "text": "添加新设备流程：在传感器数据管理页面，点击添加设备按钮，填写设备名称、IP地址等信息，保存后即可开始监控。",
        "metadata": {"category": "操作指南", "source": "系统文档"}
    },
    {
        "text": "导出报表方法：在报表配置页面，选择要导出的报表类型和时间范围，点击导出按钮即可生成PDF或Excel格式的报表文件。",
        "metadata": {"category": "操作指南", "source": "系统文档"}
    },
    {
        "text": "设置报警规则步骤：进入报警配置页面，点击添加规则按钮，配置报警ID、报警内容（中文）选择停机作为处理方式，设置完成后保存即可生效。",
        "metadata": {"category": "操作指南", "source": "系统文档"}
    }
]

print("正在初始化知识库...")

for i, item in enumerate(knowledge_base):
    # 生成向量
    embed_response = ollama.embeddings(
        model="nomic-embed-text",
        prompt=item["text"]
    )

    # 添加到集合
    collection.add(
        embeddings=[embed_response["embedding"]],
        documents=[item["text"]],
        metadatas=[item["metadata"]],
        ids=[f"doc_{i}"]
    )
    print(f"已添加文档 {i+1}/{len(knowledge_base)}")

print("知识库初始化完成！")
