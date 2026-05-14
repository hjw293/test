import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()

print("=" * 80)
print("display_text 和 text_source 表的关系分析")
print("=" * 80)

# 1. 基本统计信息
print("\n1. 基本统计信息")
print("-" * 80)

sql = "SELECT COUNT(*) FROM display_text"
cursor.execute(sql)
display_text_count = cursor.fetchone()[0]

sql = "SELECT COUNT(*) FROM text_source"
cursor.execute(sql)
text_source_count = cursor.fetchone()[0]

print(f"display_text表记录数: {display_text_count}")
print(f"text_source表记录数: {text_source_count}")

# 2. 查询display_text的字段信息
print("\n2. display_text表的结构")
print("-" * 80)

sql = "SHOW FULL COLUMNS FROM display_text"
cursor.execute(sql)
columns = cursor.fetchall()

print(f"{'字段名':<20} {'类型':<30} {'允许NULL':<10} {'注释'}")
print("-" * 70)
for col in columns:
    field = col[0] if col[0] else ''
    type_info = col[1] if col[1] else ''
    null_allowed = col[2] if col[2] else ''
    comment = col[8] if len(col) > 8 and col[8] else ''
    print(f"{field:<20} {type_info:<30} {null_allowed:<10} {comment}")

# 3. 查询text_source的字段信息
print("\n3. text_source表的结构")
print("-" * 80)

sql = "SHOW FULL COLUMNS FROM text_source"
cursor.execute(sql)
columns = cursor.fetchall()

print(f"{'字段名':<20} {'类型':<30} {'允许NULL':<10} {'注释'}")
print("-" * 70)
for col in columns:
    field = col[0] if col[0] else ''
    type_info = col[1] if col[1] else ''
    null_allowed = col[2] if col[2] else ''
    comment = col[8] if len(col) > 8 and col[8] else ''
    print(f"{field:<20} {type_info:<30} {null_allowed:<10} {comment}")

# 4. 分析关联关系
print("\n4. 关联关系分析")
print("-" * 80)

# display_text.name_id与text_source的关联
sql = """
SELECT
    COUNT(DISTINCT dt.name_id) as unique_name_ids,
    COUNT(DISTINCT ts.sub_id) as text_source_sub_ids,
    COUNT(CASE WHEN ts.sub_id IS NOT NULL THEN 1 END) as linked_records
FROM display_text dt
LEFT JOIN text_source ts ON dt.name_id = ts.sub_id
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"display_text中不同name_id数量: {result[0]}")
print(f"text_source中匹配的sub_id数量: {result[1]}")
print(f"能关联到text_source的记录数: {result[2]}")

# display_text.lang_id与text_source的关联
sql = """
SELECT
    COUNT(DISTINCT dt.lang_id) as unique_lang_ids,
    COUNT(DISTINCT ts.lang_unique_id) as text_source_lang_ids,
    COUNT(CASE WHEN ts.lang_unique_id IS NOT NULL THEN 1 END) as linked_records
FROM display_text dt
LEFT JOIN text_source ts ON dt.lang_id = ts.lang_unique_id
"""
cursor.execute(sql)
result = cursor.fetchone()
print(f"\ndisplay_text中不同lang_id数量: {result[0]}")
print(f"text_source中匹配的lang_unique_id数量: {result[1]}")
print(f"能关联到text_source的记录数: {result[2]}")

# 5. 查询实际的关联示例
print("\n5. 实际关联示例")
print("-" * 80)

# 示例1：通过name_id关联
sql = """
SELECT
    dt.name_id,
    dt.lang_id,
    dt.lang_name,
    dt.value,
    dt.text_content,
    ts.string_content as text_source_content,
    ts.type_id,
    ts.sub_id
FROM display_text dt
LEFT JOIN text_source ts ON dt.name_id = ts.sub_id
LIMIT 5
"""
cursor.execute(sql)
results = cursor.fetchall()

print("5.1 通过name_id关联的示例")
print(f"{'NameID':<10} {'LangID':<10} {'LangName':<15} {'Value':<10} {'DisplayText':<30} {'TextSource':<30}")
print("-" * 110)
for row in results:
    print(f"{row[0]:<10} {row[1]:<10} {str(row[2]):<15} {row[3]:<10} {str(row[4]):<30} {str(row[5]):<30}")

# 示例2：通过lang_id关联
sql = """
SELECT
    dt.name_id,
    dt.lang_id,
    dt.lang_name,
    dt.text_content,
    ts.string_content as text_source_content,
    ts.lang_name as ts_lang_name
FROM display_text dt
LEFT JOIN text_source ts ON dt.lang_id = ts.lang_unique_id
WHERE ts.lang_unique_id IS NOT NULL
LIMIT 5
"""
cursor.execute(sql)
results = cursor.fetchall()

print("\n5.2 通过lang_id关联的示例")
print(f"{'NameID':<10} {'LangID':<10} {'DisplayLang':<15} {'DisplayText':<30} {'TextSource':<30} {'TSLang':<15}")
print("-" * 110)
for row in results:
    print(f"{row[0]:<10} {row[1]:<10} {str(row[2]):<15} {str(row[3]):<30} {str(row[4]):<30} {str(row[5]):<15}")

# 6. 查询display_text的独特用途
print("\n6. display_text的独特用途分析")
print("-" * 80)

sql = """
SELECT
    dt.name_id,
    dt.value,
    COUNT(*) as text_variations,
    GROUP_CONCAT(DISTINCT dt.lang_name) as languages
FROM display_text dt
GROUP BY dt.name_id, dt.value
HAVING COUNT(*) > 1
LIMIT 10
"""
cursor.execute(sql)
results = cursor.fetchall()

print("按name_id和value分组的文本变体:")
print(f"{'NameID':<10} {'Value':<10} {'变体数量':<10} {'语言列表'}")
print("-" * 80)
for row in results:
    print(f"{row[0]:<10} {row[1]:<10} {row[2]:<10} {row[3]}")

# 7. 对比两表的差异
print("\n7. display_text vs text_source 对比")
print("=" * 80)
print("""
display_text表特点：
• 专门用于"按当前数值显示文字数据"
• 包含value字段，根据数值决定显示内容
• 支持同一name_id下不同value显示不同文本
• 适用于状态值、枚举值的显示

text_source表特点：
• 作为统一的文本资源中心
• 通过lang_unique_id提供多语言支持
• 包含string_content和desc_content两个字段
• 被多个表引用，存储各种文本内容

关键区别：
1. display_text：基于数值的条件显示 (name_id + value → text)
2. text_source：基于ID的直接引用 (lang_unique_id → text)
""")

# 8. 查询实际使用场景
print("\n8. 实际使用场景示例")
print("-" * 80)

sql = """
SELECT
    dt.name_id,
    dt.value,
    dt.text_content,
    ts.string_content as reference_text
FROM display_text dt
LEFT JOIN text_source ts ON dt.name_id = ts.sub_id
WHERE dt.name_id IN (SELECT name_id FROM display_text GROUP BY name_id HAVING COUNT(*) > 3)
LIMIT 10
"""
cursor.execute(sql)
results = cursor.fetchall()

print("同一name_id下不同value对应的显示文本:")
print(f"{'NameID':<10} {'Value':<10} {'DisplayText':<30} {'TextSource'}")
print("-" * 70)
for row in results:
    print(f"{row[0]:<10} {row[1]:<10} {str(row[2]):<30} {str(row[3])}")

cursor.close()
conn.close()