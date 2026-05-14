import mysql.connector
import json

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()

# 要分析的表列表（排除sensor_data和users）
tables_to_analyze = [
    'alarm_config',
    'alarm_log',
    'answer_type',
    'batch_report_config',
    'display_text',
    'display_value_text',
    'log_curve_group',
    'mapping',
    'text_source'
]

for table in tables_to_analyze:
    print(f"\n{'='*80}")
    print(f"表名: {table}")
    print(f"{'='*80}")

    # 获取表结构和注释
    cursor.execute(f'SHOW FULL COLUMNS FROM {table}')
    columns = cursor.fetchall()

    print("\n字段信息:")
    print(f"{'字段名':<25} {'类型':<30} {'允许NULL':<10} {'默认值':<15} {'注释'}")
    print("-" * 105)
    for col in columns:
        field = col[0] if col[0] else ''
        type_info = col[1] if col[1] else ''
        null_allowed = col[2] if col[2] else ''
        key = col[3] if col[3] else ''
        default = str(col[4]) if col[4] is not None else 'NULL'
        extra = col[5] if col[5] else ''
        comment = col[8] if len(col) > 8 and col[8] else ''

        print(f"{field:<25} {type_info:<30} {null_allowed:<10} {default:<15} {comment}")

    # 获取索引信息
    cursor.execute(f'SHOW INDEX FROM {table}')
    indexes = cursor.fetchall()
    if indexes:
        print("\n索引信息:")
        index_dict = {}
        for idx in indexes:
            key_name = idx[2]
            column_name = idx[4]
            if key_name not in index_dict:
                index_dict[key_name] = []
            index_dict[key_name].append(column_name)

        for key_name, columns in index_dict.items():
            print(f"  - {key_name}: {', '.join(columns)}")

    # 获取表注释
    cursor.execute(f"SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA='testdb' AND TABLE_NAME='{table}'")
    table_comment = cursor.fetchone()
    if table_comment and table_comment[0]:
        print(f"\n表注释: {table_comment[0]}")

cursor.close()
conn.close()