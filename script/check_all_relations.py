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

# 1. mapping表的关联关系
print("\n1. mapping表的所有关联关系")
print("-" * 80)

# mapping → text_source (name_id关联)
print("\n1.1 mapping.name_id → text_source")
sql = """
SELECT
    COUNT(DISTINCT m.name_id) as mapping_name_ids,
    COUNT(DISTINCT ts.type_id) as text_source_types,
    COUNT(m.id) as total_mappings
FROM mapping m
LEFT JOIN text_source ts ON m.name_id = ts.sub_id
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"mapping中不同name_id数量: {result[0]}")
print(f"关联到text_source的不同type_id数量: {result[1]}")
print(f"mapping总记录数: {result[2]}")

# mapping → text_source (access_id关联)
print("\n1.2 mapping.access_id → text_source")
sql = """
SELECT
    COUNT(DISTINCT m.access_id) as unique_access_ids,
    COUNT(m.id) as mappings_with_access,
    COUNT(DISTINCT ts.lang_unique_id) as text_source_links
FROM mapping m
LEFT JOIN text_source ts ON m.access_id = ts.lang_unique_id
WHERE m.access_id != 0
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"mapping中不同access_id数量: {result[0]}")
print(f"有access_id的mapping记录数: {result[1]}")
print(f"关联到text_source的数量: {result[2]}")

# mapping → answer_type
print("\n1.3 mapping.ans_type → answer_type")
sql = """
SELECT
    COUNT(DISTINCT m.ans_type) as unique_ans_types,
    COUNT(m.id) as mappings_with_ans_type,
    COUNT(DISTINCT at.id) as answer_types_used
FROM mapping m
INNER JOIN answer_type at ON m.ans_type = at.answer_type_id
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"mapping中不同ans_type数量: {result[0]}")
print(f"有ans_type的mapping记录数: {result[1]}")
print(f"使用的不同answer_type数量: {result[2]}")

# mapping → answer_type (列类型)
print("\n1.4 mapping.col_1_ans / col_2_ans → answer_type")
sql = """
SELECT
    COUNT(DISTINCT m.col_1_ans) as unique_col1_ans,
    COUNT(DISTINCT m.col_2_ans) as unique_col2_ans,
    COUNT(CASE WHEN m.col_1_ans != 0 THEN 1 END) as has_col1,
    COUNT(CASE WHEN m.col_2_ans != 0 THEN 1 END) as has_col2
FROM mapping m
WHERE m.col_1_ans != 0 OR m.col_2_ans != 0
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"列1答案类型数量: {result[0]}")
print(f"列2答案类型数量: {result[1]}")
print(f"有列1答案类型的记录数: {result[2]}")
print(f"有列2答案类型的记录数: {result[3]}")

# 2. answer_type表的关联关系
print("\n\n2. answer_type表的所有关联关系")
print("-" * 80)

# answer_type → text_source (unit_text_id关联)
print("\n2.1 answer_type.unit_text_id → text_source")
sql = """
SELECT
    COUNT(DISTINCT at.unit_text_id) as unique_unit_text_ids,
    COUNT(at.id) as answer_types_with_unit,
    COUNT(DISTINCT ts.lang_unique_id) as text_source_links
FROM answer_type at
LEFT JOIN text_source ts ON at.unit_text_id = ts.lang_unique_id
WHERE at.unit_text_id != 0
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"answer_type中不同unit_text_id数量: {result[0]}")
print(f"有unit_text_id的answer_type记录数: {result[1]}")
print(f"关联到text_source的数量: {result[2]}")

# answer_type → text_source (value_id关联)
print("\n2.2 answer_type.value_id → text_source")
sql = """
SELECT
    COUNT(DISTINCT at.value_id) as unique_value_ids,
    COUNT(at.id) as answer_types_with_value,
    COUNT(DISTINCT ts.lang_unique_id) as text_source_links
FROM answer_type at
LEFT JOIN text_source ts ON at.value_id = ts.lang_unique_id
WHERE at.value_id != 0
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"answer_type中不同value_id数量: {result[0]}")
print(f"有value_id的answer_type记录数: {result[1]}")
print(f"关联到text_source的数量: {result[2]}")

# answer_type → text_source (opt_id关联)
print("\n2.3 answer_type.opt_id → text_source")
sql = """
SELECT
    COUNT(DISTINCT at.opt_id) as unique_opt_ids,
    COUNT(at.id) as answer_types_with_opt,
    COUNT(DISTINCT ts.lang_unique_id) as text_source_links
FROM answer_type at
LEFT JOIN text_source ts ON at.opt_id = ts.lang_unique_id
WHERE at.opt_id != 0
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"answer_type中不同opt_id数量: {result[0]}")
print(f"有opt_id的answer_type记录数: {result[1]}")
print(f"关联到text_source的数量: {result[2]}")

# 3. 查询实际的关联示例
print("\n\n3. 实际关联示例")
print("-" * 80)

# 示例1: mapping → answer_type → text_source 的完整链路
print("\n3.1 完整关联链路示例")
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
    print(f"NameID: {result[0]}")
    print(f"→ mapping.ans_type: {result[1]}")
    print(f"→ answer_type.answer_type_name: {result[2]}")
    print(f"→ answer_type.unit_text_id: {result[3]}")
    print(f"→ text_source.unit_text: '{result[4]}'")
    print(f"→ answer_type.value_id: {result[5]}")
    print(f"→ text_source.value_text: '{result[6]}'")

# 示例2: mapping → text_source (access_id)
print("\n3.2 mapping.access_id → text_source 示例")
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

# 4. 统计整体关联情况
print("\n\n4. 整体关联统计")
print("-" * 80)

# 分别查询每个表
sql1 = """
SELECT
    'mapping表' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT name_id) as unique_name_ids,
    COUNT(DISTINCT ans_type) as unique_ans_types,
    COUNT(DISTINCT access_id) as unique_access_ids
FROM mapping
"""
cursor.execute(sql1)
result1 = cursor.fetchone()

sql2 = """
SELECT
    'answer_type表' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT answer_type_id) as unique_name_ids,
    COUNT(DISTINCT value_id) as unique_ans_types,
    COUNT(DISTINCT unit_text_id) as unique_access_ids
FROM answer_type
"""
cursor.execute(sql2)
result2 = cursor.fetchone()

sql3 = """
SELECT
    'text_source表' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT lang_unique_id) as unique_name_ids,
    COUNT(DISTINCT type_id) as unique_ans_types,
    0 as unique_access_ids
FROM text_source
"""
cursor.execute(sql3)
result3 = cursor.fetchone()

results = [result1, result2, result3]
print(f"{'表名':<15} {'总记录数':<12} {'唯一标识':<12} {'关联标识1':<12} {'关联标识2':<12}")
print("-" * 65)
for row in results:
    print(f"{row[0]:<15} {row[1]:<12} {row[2]:<12} {row[3]:<12} {row[4]:<12}")

print("\n\n关联关系总结：")
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
""")

cursor.close()
conn.close()