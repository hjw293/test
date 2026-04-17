import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()

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

# 查询实际关联示例
print("\n3. 实际关联示例")
print("-" * 80)

sql = """
SELECT
    m.name_id,
    m.ans_type,
    at.answer_type_name,
    at.unit_text_id,
    ts.string_content as unit_text,
    at.value_id,
    ts_value.string_content as value_text
FROM mapping m
INNER JOIN answer_type at ON m.ans_type = at.answer_type_id
LEFT JOIN text_source ts ON at.unit_text_id = ts.lang_unique_id
LEFT JOIN text_source ts_value ON at.value_id = ts_value.lang_unique_id
WHERE m.id = 100
"""
cursor.execute(sql)
result = cursor.fetchone()
if result:
    print(f"完整关联链路示例:")
    print(f"NameID: {result[0]}")
    print(f"→ mapping.ans_type: {result[1]}")
    print(f"→ answer_type.answer_type_name: {result[2]}")
    print(f"→ answer_type.unit_text_id: {result[3]}")
    print(f"→ text_source.unit_text: '{result[4]}'")
    print(f"→ answer_type.value_id: {result[5]}")
    print(f"→ text_source.value_text: '{result[6]}'")

# mapping.access_id关联示例
print(f"\nmapping.access_id → text_source 示例:")
sql = """
SELECT
    m.name_id,
    m.access_id,
    ts.string_content as access_text,
    ts.type_id as access_type
FROM mapping m
LEFT JOIN text_source ts ON m.access_id = ts.lang_unique_id
WHERE m.access_id != 0 AND ts.string_content IS NOT NULL
LIMIT 5
"""
cursor.execute(sql)
results = cursor.fetchall()
print(f"{'NameID':<10} {'AccessID':<10} {'AccessText':<30} {'Type'}")
print("-" * 60)
for row in results:
    print(f"{row[0]:<10} {row[1]:<10} {str(row[2]):<30} {row[3]}")

cursor.close()
conn.close()