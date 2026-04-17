import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()

print("=" * 80)
print("display_text 和 text_source 的准确关系分析")
print("=" * 80)

# 1. 统计display_text的语言分布
print("\n1. display_text的多语言支持")
print("-" * 80)

sql = """
SELECT
    lang_name,
    COUNT(*) as record_count,
    COUNT(DISTINCT name_id) as unique_name_ids
FROM display_text
GROUP BY lang_name
ORDER BY record_count DESC
"""
cursor.execute(sql)
results = cursor.fetchall()

print(f"{'语言名称':<20} {'记录数':<10} {'不同NameID数量':<15}")
print("-" * 50)
for row in results:
    print(f"{str(row[0]):<20} {row[1]:<10} {row[2]:<15}")

# 2. 查询text_source的类型分布
print("\n2. text_source的type_id分布")
print("-" * 80)

sql = """
SELECT
    type_id,
    COUNT(*) as record_count,
    COUNT(DISTINCT lang_unique_id) as unique_lang_ids
FROM text_source
GROUP BY type_id
ORDER BY type_id
LIMIT 20
"""
cursor.execute(sql)
results = cursor.fetchall()

print(f"{'TypeID':<10} {'记录数':<10} {'不同LangUniqueID数量':<20}")
print("-" * 45)
for row in results:
    print(f"{row[0]:<10} {row[1]:<10} {row[2]:<20}")

# 3. 分析display_text的实际用途
print("\n3. display_text的value字段分析")
print("-" * 80)

sql = """
SELECT
    value,
    COUNT(*) as count,
    COUNT(DISTINCT name_id) as unique_name_ids
FROM display_text
GROUP BY value
ORDER BY value
LIMIT 10
"""
cursor.execute(sql)
results = cursor.fetchall()

print(f"{'Value值':<10} {'记录数':<10} {'不同NameID数量':<15}")
print("-" * 40)
for row in results:
    print(f"{row[0]:<10} {row[1]:<10} {row[2]:<15}")

# 4. 查询具体的使用示例
print("\n4. 具体使用示例：多语言状态显示")
print("-" * 80)

sql = """
SELECT
    name_id,
    lang_name,
    value,
    text_content
FROM display_text
WHERE name_id = 983240
ORDER BY lang_name
"""
cursor.execute(sql)
results = cursor.fetchall()

print(f"{'NameID':<10} {'语言':<20} {'Value':<8} {'显示文本'}")
print("-" * 70)
for row in results:
    print(f"{row[0]:<10} {str(row[1]):<20} {row[2]:<8} {row[3]}")

# 5. 查询不同value的显示文本
print("\n5. 不同value值的显示文本示例")
print("-" * 80)

sql = """
SELECT
    name_id,
    value,
    text_content,
    lang_name
FROM display_text
WHERE name_id = 983250
ORDER BY value, lang_name
LIMIT 15
"""
cursor.execute(sql)
results = cursor.fetchall()

print(f"{'NameID':<10} {'Value':<8} {'语言':<20} {'显示文本'}")
print("-" * 70)
for row in results:
    print(f"{row[0]:<10} {row[1]:<8} {str(row[2]):<20} {row[3]}")

# 6. 对比分析
print("\n6. display_text 和 text_source 的对比分析")
print("=" * 80)

# 统计display_text的name_id覆盖范围
sql = "SELECT COUNT(DISTINCT name_id) FROM display_text"
cursor.execute(sql)
display_name_count = cursor.fetchone()[0]

# 统计text_source的覆盖范围
sql = "SELECT COUNT(DISTINCT sub_id) FROM text_source"
cursor.execute(sql)
text_sub_count = cursor.fetchone()[0]

sql = "SELECT COUNT(DISTINCT lang_unique_id) FROM text_source"
cursor.execute(sql)
text_lang_count = cursor.fetchone()[0]

print(f"display_text中不同name_id数量: {display_name_count}")
print(f"text_source中不同sub_id数量: {text_sub_count}")
print(f"text_source中不同lang_unique_id数量: {text_lang_count}")

print("\n\n总结：两表的关系和差异")
print("=" * 80)
print("""
display_text表的特点：
┌─────────────────────────────────────────────────────────────┐
│ • 专门用于"按当前数值显示文字数据"                          │
│ • 包含value字段，支持同一name_id下不同值显示不同文本        │
│ • 内置多语言支持（7种语言）                                │
│ • 主要用途：状态值、枚举值的条件显示                       │
│ • 数据结构：name_id + lang_name + value → text_content     │
└─────────────────────────────────────────────────────────────┘

text_source表的特点：
┌─────────────────────────────────────────────────────────────┐
│ • 作为统一的文本资源中心                                    │
│ • 通过type_id和sub_id提供结构化存储                        │
│ • 包含string_content和desc_content两个字段                 │
│ • 被多个表引用，存储各种类型的文本                         │
│ • 数据结构：lang_unique_id + type_id + sub_id → text       │
└─────────────────────────────────────────────────────────────┘

两者的主要差异：
┌─────────────────────────────────────────────────────────────┐
│ 1. 用途不同：                                               │
│    - display_text：基于数值的条件显示                       │
│    - text_source：基于ID的直接引用                         │
│                                                             │
│ 2. 关联方式不同：                                           │
│    - display_text：name_id + value → text                  │
│    - text_source：lang_unique_id → text                    │
│                                                             │
│ 3. 适用场景不同：                                           │
│    - display_text：状态显示、枚举值转换                     │
│    - text_source：通用文本资源、多语言支持                  │
│                                                             │
│ 4. 多语言实现不同：                                         │
│    - display_text：内置lang_name字段                       │
│    - text_source：通过lang_unique_id关联                   │
└─────────────────────────────────────────────────────────────┘

实际使用场景：
┌─────────────────────────────────────────────────────────────┐
│ display_text使用场景：                                      │
│ • 设备状态显示（如：运行/停止/故障）                        │
│ • 数值范围对应的文本（如：正常/警告/危险）                  │
│ • 枚举值翻译（如：0=开，1=关）                              │
│                                                             │
│ text_source使用场景：                                       │
│ • 界面标签文本                                              │
│ • 菜单和按钮文本                                            │
│ • 错误消息和提示信息                                        │
│ • 各种配置项的描述                                          │
└─────────────────────────────────────────────────────────────┘
""")

cursor.close()
conn.close()