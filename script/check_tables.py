import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='123456',
    database='testdb'
)

cursor = conn.cursor()
cursor.execute('SHOW TABLES')
tables = [table[0] for table in cursor.fetchall()]

print('表详细信息：\n')
for table in tables:
    cursor.execute(f'SELECT COUNT(*) FROM {table}')
    count = cursor.fetchone()[0]
    print(f'{table}: {count} 条记录')

cursor.close()
conn.close()