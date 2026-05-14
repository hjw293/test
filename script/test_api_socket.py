import socket
import json

print("测试后端 API...")

try:
    # 创建 socket 连接
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout(10)
    sock.connect(('127.0.0.1', 8080))

    # 发送 HTTP 请求
    request = "GET /api/data HTTP/1.1\r\n"
    request += "Host: 127.0.0.1\r\n"
    request += "Accept: application/json\r\n"
    request += "Connection: close\r\n"
    request += "\r\n"

    sock.send(request.encode())

    # 接收响应
    response = b""
    while True:
        chunk = sock.recv(4096)
        if not chunk:
            break
        response += chunk

    sock.close()

    # 解析响应
    response_str = response.decode('utf-8')
    print(f"响应长度: {len(response_str)} 字节")
    print(f"响应内容前500字符: {response_str[:500]}")

    # 解析 HTTP 响应
    if response_str.startswith('HTTP'):
        headers, body = response_str.split('\r\n\r\n', 1)
        print(f"\nHTTP 响应头:\n{headers}")

        # 解析 JSON
        try:
            data = json.loads(body)
            print(f"\nJSON 数据结构: {json.dumps(data, indent=2, ensure_ascii=False)[:1000]}")
        except json.JSONDecodeError as e:
            print(f"\nJSON 解析失败: {e}")
            print(f"原始数据: {body[:500]}")

except socket.timeout:
    print("连接超时")
except Exception as e:
    print(f"错误: {e}")