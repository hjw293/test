# -*- coding: utf-8 -*-
import requests
import json

# Test the API
resp = requests.post('http://localhost:8000/api/rag/chat',
                     json={'question': '其中自动应答的有多少个'},
                     timeout=60)
print('Status:', resp.status_code)
data = resp.json()
print('Answer:', data.get('answer'))
print('Sources:', data.get('sources'))
print('Function called:', data.get('function_called'))