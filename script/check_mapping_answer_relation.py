import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()

print("=" * 80)
print("验证 mapping 和 answer_type 的关联关系")
print("=" * 80)

# 1. 检查 mapping.ans_type 与 answer_type.answer_type_id 的关联
print("\n1. mapping.ans_type → answer_type.answer_type_id 关联检查")
print("-" * 80)

sql = """
SELECT
    m.id as mapping_id,
    m.name_id,
    m.ans_type,
    at.id as answer_type_id,
    at.answer_type_name,
    at.format_type,
    at.default_value
FROM mapping m
LEFT JOIN answer_type at ON m.ans_type = at.answer_type_id
LIMIT 10
"""
cursor.execute(sql)
results = cursor.fetchall()
print(f"{'MappingID':<10} {'NameID':<10} {'MappingAnsType':<15} {'AnswerTypeID':<15} {'TypeName':<30} {'Format':<10} {'Default':<10}")
print("-" * 100)
for row in results:
    mapping_id, name_id, m_ans_type, at_id, at_name, at_format, at_default = row
    print(f"{mapping_id:<10} {name_id:<10} {m_ans_type:<15} {at_id:<15} {str(at_name):<30} {at_format:<10} {at_default:<10}")

# 2. 统计关联数量
print("\n\n2. 关联统计")
print("-" * 80)

cursor.execute("SELECT COUNT(*) FROM mapping WHERE ans_type IS NOT NULL")
mapping_with_ans_type = cursor.fetchone()[0]
print(f"mapping表中ans_type不为空的记录数: {mapping_with_ans_type}")

cursor.execute("SELECT COUNT(*) FROM answer_type")
total_answer_types = cursor.fetchone()[0]
print(f"answer_type表中的总记录数: {total_answer_types}")

cursor.execute("""
SELECT COUNT(DISTINCT m.ans_type)
FROM mapping m
INNER JOIN answer_type at ON m.ans_type = at.answer_type_id
""")
linked_ans_types = cursor.fetchone()[0]
print(f"mapping表关联到的不同answer_type数量: {linked_ans_types}")

# 3. 检查列答案类型的关联
print("\n\n3. 列答案类型关联检查")
print("-" * 80)

cursor.execute("""
SELECT
    m.id,
    m.col_1_ans,
    m.col_2_ans,
    at1.answer_type_name as col1_name,
    at2.answer_type_name as col2_name
FROM mapping m
LEFT JOIN answer_type at1 ON m.col_1_ans = at1.answer_type_id
LEFT JOIN answer_type at2 ON m.col_2_ans = at2.answer_type_id
WHERE m.col_1_ans IS NOT NULL OR m.col_2_ans IS NOT NULL
LIMIT 10
""")
results = cursor.fetchall()
print(f"{'MappingID':<10} {'Col1AnsType':<12} {'Col2AnsType':<12} {'Col1Name':<30} {'Col2Name':<30}")
print("-" * 100)
for row in results:
    mapping_id, col1_ans, col2_ans, col1_name, col2_name = row
    print(f"{mapping_id:<10} {col1_ans:<12} {col2_ans:<12} {str(col1_name):<30} {str(col2_name):<30}")

# 4. 显示具体的关联示例
print("\n\n4. 具体关联示例")
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
    at.translation_offset1
FROM mapping m
INNER JOIN answer_type at ON m.ans_type = at.answer_type_id
LIMIT 5
"""
cursor.execute(sql)
results = cursor.fetchall()
for row in results:
    print(f"\nNameID: {row[0]}")
    print(f"  Mapping.ans_type: {row[1]}")
    print(f"  Answer Type: {row[2]}")
    print(f"  Format Type: {row[3]} (0=数值转换, 1=选项转换, 2=时间转换, 3=颜色转换)")
    print(f"  数字显示: {row[4]}位整数, {row[5]}位小数")
    print(f"  转换公式: (原始值 × {row[6]} ÷ {row[7]}) + {row[8]}")

cursor.close()
conn.close()