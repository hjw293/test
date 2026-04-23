# rag_service/init_knowledge.py
import ollama
import chromadb

# 初始化 ChromaDB（使用 PersistentClient 持久化存储）
chroma_client = chromadb.PersistentClient(path="./chroma_db")

# 删除已存在的集合并重新创建
try:
    chroma_client.delete_collection(name="sensor_knowledge")
except:
    pass

# 创建集合
collection = chroma_client.create_collection(
    name="sensor_knowledge",
    metadata={"description": "染整监控系统知识库"}
)

# 知识库内容
knowledge_base = [
    # 功能介绍
    {
        "text": "传感器数据监控平台用于实时展示染整设备的传感器数据，支持多设备管理和数据可视化。用户可以查看设备数据曲线、对比分析和历史记录。",
        "metadata": {"category": "功能介绍", "source": "系统文档"}
    },
    {
        "text": "报警配置用于设置设备报警规则。当设备状态异常（如停机）时，系统会自动触发报警通知。报警规则包括报警ID、报警内容、处理方式等配置。",
        "metadata": {"category": "报警功能", "source": "系统文档"}
    },
    {
        "text": "曲线组功能用于分组管理和展示设备传感器曲线。用户可以创建多个曲线组，每个组包含多个设备的传感器数据进行对比分析。",
        "metadata": {"category": "曲线功能", "source": "系统文档"}
    },
    {
        "text": "设备停机状态说明：当设备报警配置的处理方式为'停机'时，表示该设备当前处于停机状态，需要及时处理。停机可能原因包括：设备故障、维护保养、生产计划等。",
        "metadata": {"category": "设备状态", "source": "系统文档"}
    },

    # ========== 报表配置系统 ==========
    {
        "text": "批量报表配置管理系统用于管理染整监控系统的各类报表配置，包括能耗报告和健康指数报告两大类别。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "报告类别分为两类：0=能耗报告，用于展示能源消耗数据；1=健康指数报告，用于展示设备健康状态。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "报表类型有哪几种？报表系统支持两种报告类型：能耗报告和健康指数报告。能耗报告展示能源消耗数据，健康指数报告展示设备健康状态。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "系统提供两种报告：能耗报告和健康指数报告。能耗报告用于查看能源消耗情况，健康指数报告用于查看设备健康状态。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "有多少种报告？报表配置系统支持两种报告类型，分别是能耗报告和健康指数报告。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "项目类型分为三种：0=项目，表示单个项目；1=项目标题，表示项目下的标题；2=标题，表示单纯的标题。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "名称ID（nameId）是报表配置的核心标识，用于关联具体的报表名称和功能图示。每个报表配置都有唯一的nameId。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "功能图示（fi_xxxx.png）用于展示报表的功能图标，通过nameId关联。例如访问/figure/fi_123.png可获取nameId=123的功能图。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "报告图示（ri_xxxx.png）用于展示报表的报告图标，通过imageId关联。如果imageId=0则不显示图示。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "启用模式有两种：enableNameId=0表示永久启动，即该报表始终生效；enableNameId非0表示条件启动，只有当对应nameId的值非零时才能启动。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "参考Name ID（refNameId）用于设置报表的参考指标，可以关联其他报表数据进行对比分析。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "无效标识用于处理特殊数值。当enableInvalidMarker=1启用时，如果数值等于invalidValue设置的值，则显示'-----'而不是原数值。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "步骤参数ID（stepParamId）用于关联具体的工艺步骤参数，便于追踪每个报表对应的生产工艺环节。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "报表配置支持按报告类别（reportCategory）、项目类型（reportType）和名称ID（nameId）进行筛选查询。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "报表配置列表采用分页展示，每页默认显示12条记录，支持切换每页显示数量（12、24、48条）。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "点击报表卡片可以查看详细信息，包括基本信息、启动配置和其他配置参数。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "名称文本（nameText）是从text_source表关联查询得到的报表显示名称，作为报表卡片的标题展示。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "报表统计功能可以统计系统中能耗报告和健康指数报告的具体数量，帮助用户了解报表配置的整体情况。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    {
        "text": "如何查询报表数量？系统支持统计报表配置的数量，包括能耗报告数量和健康指数报告数量，方便用户了解报表配置情况。",
        "metadata": {"category": "报表配置", "source": "系统文档"}
    },
    # ==================================

    # 操作指南
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
