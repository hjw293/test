# -*- coding: utf-8 -*-
import re
import sys

question = '其中自动应答的有多少个'
print('Original:', question)
print('Lower:', question.lower())

q_lower = question.lower()
match = re.search(r'其中(.+?)有多少', q_lower)
if match:
    specific = match.group(1).strip()
    print('Extracted:', specific)

    # Test exclusion
    excluded = ['设备', '报表', '报告', '能耗', '健康']
    if any(k in specific for k in excluded):
        print('Would be excluded')
    else:
        print('Would call API')
else:
    print('No match')