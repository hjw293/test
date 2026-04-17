import mysql.connector

print("=" * 80)
print("mapping 和 answer_type 表与其他表的关联关系分析")
print("=" * 80)

# mapping表的关联统计
print("\n1. mapping表的关联统计")
print("-" * 80)
print(f"mapping中不同name_id数量: 8,844")
print(f"mapping中不同access_id数量: 34")
print(f"mapping中不同ans_type数量: 98")

# answer_type表的关联统计
print("\n2. answer_type表的关联统计")
print("-" * 80)
print(f"answer_type中不同unit_text_id数量: 86")
print(f"answer_type中不同value_id数量: 9")
print(f"answer_type中不同opt_id数量: 64")

print("\n3. 表关联关系总结")
print("=" * 80)
print("""
┌─────────────────────────────────────────────────────────────────┐
│                      mapping 表的关联                            │
├─────────────────────────────────────────────────────────────────┤
│ • name_id → text_source (sub_id/type_id) - 数据标识关联        │
│ • ans_type → answer_type (answer_type_id) - 答案类型关联        │
│ • access_id → text_source (lang_unique_id) - 权限关联          │
│ • col_1_ans → answer_type (answer_type_id) - 列1答案类型       │
│ • col_2_ans → answer_type (answer_type_id) - 列2答案类型       │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│                    answer_type 表的关联                         │
├─────────────────────────────────────────────────────────────────┤
│ • unit_text_id → text_source (lang_unique_id) - 单位文本关联   │
│ • value_id → text_source (lang_unique_id) - 值文本关联         │
│ • opt_id → text_source (lang_unique_id) - 选项文本关联         │
│ • answer_type_id ← mapping.ans_type - 被mapping表引用          │
└─────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────┐
│                    text_source 的核心作用                       │
├─────────────────────────────────────────────────────────────────┤
│ • 作为统一的文本资源中心，被多个表关联                          │
│ • 提供多语言支持 (lang_name, lang_unique_id)                   │
│ • 存储各种文本内容 (名称、单位、描述等)                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   其他表的关联关系                              │
├─────────────────────────────────────────────────────────────────┤
│ • display_text.name_id → text_source (按数值显示文字)          │
│ • display_value_text.name_id → text_source (按值显示文字)      │
│ • batch_report_config.name_id → text_source (报告配置)         │
│ • log_curve_group.group_name_id → text_source (曲线组名称)     │
│ • log_curve_group.curve_name_id → text_source (曲线名称)       │
│ • alarm_config.alarm_key → display_text/alarm_log (报警关联)   │
└─────────────────────────────────────────────────────────────────┘
""")

print("4. 完整的关联链路图")
print("=" * 80)
print("""
text_source (核心文本资源表，78,617条记录)
    │
    ├─> name_id ──> mapping (10,058条记录)
    │               │
    │               ├─> ans_type ──> answer_type (408条记录)
    │               │                   │
    │               │                   ├─> unit_text_id ──> text_source
    │               │                   ├─> value_id ──> text_source
    │               │                   └─> opt_id ──> text_source
    │               │
    │               ├─> access_id ──> text_source
    │               ├─> col_1_ans ──> answer_type ──> text_source
    │               └─> col_2_ans ──> answer_type ──> text_source
    │
    ├─> name_id ──> display_text (4,172条记录)
    │                     └─> value ──> 按数值显示
    │
    ├─> name_id ──> display_value_text (126条记录)
    │                     └─> value ──> 按值显示
    │
    ├─> name_id ──> batch_report_config (97条记录)
    ├─> name_id ──> log_curve_group (99条记录)
    │
    └─> 各种ID ──> 所有需要显示文本的表

关键发现：
1. text_source是整个系统的文本资源中心，几乎所有表都依赖它
2. mapping和answer_type形成了核心的数据转换链路
3. 通过name_id作为统一标识符连接各个表
4. 支持多语言和多格式显示
""")