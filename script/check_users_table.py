import mysql.connector

try:
    conn = mysql.connector.connect(host='localhost', user='root', password='123456', database='testdb')
    cursor = conn.cursor()
    cursor.execute('SHOW TABLES LIKE "users"')
    result = cursor.fetchone()
    if result:
        print('用户表存在')
        cursor.execute('SELECT COUNT(*) FROM users')
        count = cursor.fetchone()[0]
        print(f'用户数量: {count}')
        cursor.execute('SELECT username FROM users LIMIT 5')
        users = cursor.fetchall()
        print('用户列表:', [user[0] for user in users])
    else:
        print('用户表不存在')
    conn.close()
except Exception as e:
    print(f'错误: {e}')