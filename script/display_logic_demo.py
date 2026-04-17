import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()

print("=" * 80)
print("answer_type 和 mapping 表的展示逻辑分析")
print("=" * 80)

# 1. 查询不同格式的answer_type示例
print("\n1. Answer Type 转换规则示例")
print("-" * 80)

sql = """
SELECT
    at.answer_type_id,
    at.answer_type_name,
    at.format_type,
    CASE at.format_type
        WHEN 0 THEN '数值转换'
        WHEN 1 THEN '选项转换'
        WHEN 2 THEN '时间转换'
        WHEN 3 THEN '颜色转换'
    END as format_desc,
    at.unit_number_of_digital,
    at.unit_number_of_decimal,
    at.translation_factor1,
    at.translation_divider1,
    at.translation_offset1
FROM answer_type at
ORDER BY at.format_type, at.answer_type_name
LIMIT 15
"""
cursor.execute(sql)
results = cursor.fetchall()
print(f"{'ID':<5} {'名称':<20} {'格式类型':<15} {'格式描述':<10} {'整数位':<8} {'小数位':<8} {'转换公式'}")
print("-" * 100)
for row in results:
    ans_id, name, fmt_type, fmt_desc, digits, decimals, factor, divider, offset = row
    formula = f"({factor}×val÷{divider})+{offset}"
    print(f"{ans_id:<5} {str(name):<20} {fmt_type:<15} {fmt_desc:<10} {digits:<8} {decimals:<8} {formula}")

# 2. 查询mapping表的配置示例
print("\n\n2. Mapping 配置示例")
print("-" * 80)

sql = """
SELECT
    m.id,
    m.name_id,
    m.ans_type,
    at.answer_type_name,
    m.max_value,
    m.min_value,
    m.default_value,
    m.access_id
FROM mapping m
LEFT JOIN answer_type at ON m.ans_type = at.answer_type_id
WHERE m.ans_type IS NOT NULL
ORDER BY m.id
LIMIT 10
"""
cursor.execute(sql)
results = cursor.fetchall()
print(f"{'ID':<5} {'NameID':<10} {'AnsType':<8} {'AnsTypeName':<20} {'最小值':<12} {'最大值':<12} {'默认值':<12} {'权限ID':<8}")
print("-" * 95)
for row in results:
    m_id, name_id, ans_type, ans_name, min_val, max_val, def_val, access_id = row
    print(f"{m_id:<5} {name_id:<10} {ans_type:<8} {str(ans_name):<20} {min_val:<12} {max_val:<12} {def_val:<12} {access_id:<8}")

# 3. 模拟实际展示逻辑
print("\n\n3. 模拟实际展示逻辑")
print("-" * 80)

print("场景：系统接收到一个原始数值，需要显示在页面上")
print("\n步骤1：从mapping表获取该name_id的配置")
print("步骤2：从answer_type表获取转换规则")
print("步骤3：应用转换公式")
print("步骤4：格式化显示")

sql = """
SELECT
    m.name_id,
    m.ans_type,
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
WHERE m.id = 100
"""
cursor.execute(sql)
result = cursor.fetchone()

if result:
    name_id, ans_type, default_val, ans_name, fmt_type, digits, decimals, factor, divider, offset, unit = result
    print(f"\n示例配置 (Mapping ID: 100):")
    print(f"  NameID: {name_id}")
    print(f"  答案类型: {ans_name} (ID: {ans_type})")
    print(f"  格式类型: {fmt_type}")
    print(f"  默认值: {default_val}")
    print(f"  单位: {unit}")

    print(f"\n转换规则:")
    print(f"  转换公式: 显示值 = (原始值 × {factor} ÷ {divider}) + {offset}")
    print(f"  显示格式: {digits}位整数.{decimals}位小数")

    print(f"\n实际转换示例:")
    test_values = [0, 100, 500, 1000, default_val]
    for raw_val in test_values:
        converted = (raw_val * factor / divider) + offset
        formatted = f"{'{0:.{1}f}'.format(converted, decimals)}" if decimals > 0 else f"{int(converted)}"
        display_value = formatted if unit else formatted
        print(f"  原始值 {raw_val:>6} → 显示值 {display_value:<15}")

# 4. 列类型展示逻辑
print("\n\n4. 列类型展示逻辑")
print("-" * 80)

sql = """
SELECT
    m.id,
    m.name_id,
    m.col_1_ans,
    m.col_2_ans,
    at1.answer_type_name as col1_name,
    at2.answer_type_name as col2_name,
    m.col_1_text,
    m.col_2_text,
    ts1.string_content as col1_text,
    ts2.string_content as col2_text
FROM mapping m
LEFT JOIN answer_type at1 ON m.col_1_ans = at1.answer_type_id
LEFT JOIN answer_type at2 ON m.col_2_ans = at2.answer_type_id
LEFT JOIN text_source ts1 ON m.col_1_text = ts1.lang_unique_id
LEFT JOIN text_source ts2 ON m.col_2_text = ts2.lang_unique_id
WHERE m.col_1_ans IS NOT NULL AND m.col_2_ans IS NOT NULL
LIMIT 5
"""
cursor.execute(sql)
results = cursor.fetchall()
print(f"{'MappingID':<10} {'NameID':<10} {'列1答案类型':<15} {'列2答案类型':<15} {'列1标题':<20} {'列2标题':<20}")
print("-" * 90)
for row in results:
    m_id, name_id, col1_ans, col2_ans, col1_name, col2_name, col1_text_id, col2_text_id, col1_text, col2_text = row
    print(f"{m_id:<10} {name_id:<10} {str(col1_name):<15} {str(col2_name):<15} {str(col1_text):<20} {str(col2_text):<20}")

print("\n\n总结：")
print("-" * 80)
print("1. mapping表：定义'这是什么数据'（范围、默认值、权限、答案类型）")
print("2. answer_type表：定义'如何显示这个数据'（转换规则、格式、单位）")
print("3. 展示逻辑：mapping → answer_type → 转换计算 → 格式化显示")

cursor.close()
conn.close()