import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()

print("=" * 80)
print("最终展示逻辑示例：温度传感器数据")
print("=" * 80)

# 找一个有实际意义的配置（比如温度相关的）
sql = """
SELECT
    m.id as mapping_id,
    m.name_id,
    m.ans_type,
    m.max_value,
    m.min_value,
    m.default_value,
    at.answer_type_name,
    at.format_type,
    at.unit_number_of_digital,
    at.unit_number_of_decimal,
    at.translation_factor1,
    at.translation_divider1,
    at.translation_offset1,
    ts.string_content as unit_text
FROM mapping m
INNER JOIN answer_type at ON m.ans_type = at.answer_type_id
LEFT JOIN text_source ts ON at.unit_text_id = ts.lang_unique_id
WHERE at.answer_type_name LIKE '%C%' OR at.answer_type_name LIKE '%温度%'
LIMIT 3
"""
cursor.execute(sql)
results = cursor.fetchall()

print("\n1. 查询到的温度相关配置：")
print("-" * 80)
for row in results:
    m_id, name_id, ans_type, max_val, min_val, default_val, ans_name, fmt_type, digits, decimals, factor, divider, offset, unit = row
    print(f"\n配置ID: {m_id}")
    print(f"NameID: {name_id}")
    print(f"答案类型: {ans_name} (ID: {ans_type})")
    print(f"数值范围: {min_val} ~ {max_val}")
    print(f"默认值: {default_val}")
    print(f"单位: {unit if unit else '无单位'}")
    print(f"显示格式: {digits}位整数.{decimals}位小数")
    print(f"转换公式: (原始值 × {factor} ÷ {divider}) + {offset}")

# 创建一个更完整的展示示例
print("\n\n2. 完整的页面展示逻辑流程：")
print("-" * 80)

sql = """
SELECT
    m.name_id,
    m.ans_type,
    at.answer_type_name,
    at.format_type,
    at.unit_number_of_digital,
    at.unit_number_of_decimal,
    at.translation_factor1,
    at.translation_divider1,
    at.translation_offset1,
    ts.string_content as unit_text
FROM mapping m
INNER JOIN answer_type at ON m.ans_type = at.answer_type_id
LEFT JOIN text_source ts ON at.unit_text_id = ts.lang_unique_id
WHERE m.id = 150
"""
cursor.execute(sql)
result = cursor.fetchone()

if result:
    name_id, ans_type, ans_name, fmt_type, digits, decimals, factor, divider, offset, unit = result

    print("步骤1：系统接收到原始数据")
    print("  原始值: 25")
    print("  NameID: {}".format(name_id))

    print("\n步骤2：从mapping表获取基本配置")
    print("  查询mapping表找到对应的ans_type: {}".format(ans_type))

    print("\n步骤3：从answer_type表获取转换规则")
    print("  答案类型名称: {}".format(ans_name))
    print("  转换公式: (原始值 × {} ÷ {}) + {}".format(factor, divider, offset))

    print("\n步骤4：执行转换计算")
    raw_value = 25
    converted_value = (raw_value * factor / divider) + offset
    print("  转换值: {} × {} ÷ {} + {} = {}".format(raw_value, factor, divider, offset, converted_value))

    print("\n步骤5：格式化显示")
    if decimals > 0:
        formatted_value = "{:.{}f}".format(converted_value, decimals)
    else:
        formatted_value = str(int(converted_value))

    display_result = formatted_value + (" " + unit if unit else "")
    print("  格式化: {}位整数.{}位小数".format(digits, decimals))
    print("  最终显示: '{}'".format(display_result))

print("\n\n3. 不同转换格式的对比：")
print("-" * 80)

# 获取几个不同类型的answer_type
sql = """
SELECT
    at.answer_type_name,
    at.format_type,
    CASE at.format_type
        WHEN 0 THEN '数值转换'
        WHEN 1 THEN '选项转换'
        WHEN 2 THEN '时间转换'
        WHEN 3 THEN '颜色转换'
    END as format_desc,
    at.translation_factor1,
    at.translation_divider1,
    at.translation_offset1,
    ts.string_content as unit_text
FROM answer_type at
LEFT JOIN text_source ts ON at.unit_text_id = ts.lang_unique_id
WHERE at.format_type IN (0, 1, 2)
ORDER BY at.format_type, at.answer_type_name
LIMIT 8
"""
cursor.execute(sql)
results = cursor.fetchall()
print(f"{'类型名称':<20} {'格式类型':<15} {'格式描述':<15} {'转换参数'}")
print("-" * 80)
for row in results:
    name, fmt_type, fmt_desc, factor, divider, offset, unit = row
    params = f"×{factor}÷{divider}+{offset}"
    print(f"{str(name):<20} {fmt_type:<15} {fmt_desc:<15} {params}")

print("\n\n总结：表格在页面上的展示层次")
print("=" * 80)
print("""
┌─────────────────────────────────────────────────────────┐
│                    页面展示层                            │
│  ├─ 表头标题 (来自text_source)                           │
│  ├─ 数据值 (经过answer_type转换后)                       │
│  └─ 单位 (来自text_source)                              │
└─────────────────────────────────────────────────────────┘
                            ↑
                            │
┌─────────────────────────────────────────────────────────┐
│                  answer_type (展示层)                   │
│  ├─ 转换公式 (如何计算显示值)                            │
│  ├─ 显示格式 (小数位数、整数位数)                        │
│  ├─ 单位关联 (unit_text_id → text_source)               │
│  └─ 格式类型 (数值/选项/时间/颜色)                      │
└─────────────────────────────────────────────────────────┘
                            ↑
                            │
┌─────────────────────────────────────────────────────────┐
│                    mapping (参考层)                     │
│  ├─ 数据范围 (max_value, min_value)                     │
│  ├─ 默认值 (default_value)                              │
│  ├─ 权限控制 (access_id)                                │
│  └─ 答案类型关联 (ans_type → answer_type)               │
└─────────────────────────────────────────────────────────┘
                            ↑
                            │
┌─────────────────────────────────────────────────────────┐
│                   原始数据输入                           │
│  ├─ name_id (数据标识)                                  │
│  └─ raw_value (原始数值)                                │
└─────────────────────────────────────────────────────────┘
""")

print("关键区别：")
print("-" * 80)
print("mapping表是\"参考配置\"：定义数据的基本属性和范围")
print("answer_type表是\"展示规则\"：定义如何将原始数据转换为用户可读的显示")
print("展示逻辑：原始数据 → mapping配置 → answer_type转换 → 页面显示")

cursor.close()
conn.close()