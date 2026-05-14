import requests
import json
import time

print("测试后端 API...")

try:
    start_time = time.time()
    response = requests.get('http://localhost:8080/api/data', timeout=30)
    duration = time.time() - start_time

    print(f"状态码: {response.status_code}")
    print(f"响应时间: {duration:.2f}秒")
    print(f"响应头: {dict(response.headers)}")
    print(f"响应内容: {response.text[:500]}")

    if response.status_code == 200:
        data = response.json()
        print(f"\n数据结构: {json.dumps(data, indent=2, ensure_ascii=False)[:1000]}")

        if data.get('code') == 200:
            sensor_data = data.get('data', {})
            print(f"\n设备数量: {len(sensor_data)}")
            for device, items in sensor_data.items():
                print(f"  {device}: {len(items)} 条数据")
        else:
            print(f"\n错误: {data.get('message')}")
    else:
        print(f"\n请求失败: {response.status_code}")

except requests.exceptions.Timeout:
    print("请求超时")
except requests.exceptions.ConnectionError:
    print("连接失败，请检查后端是否运行")
except Exception as e:
    print(f"错误: {e}")