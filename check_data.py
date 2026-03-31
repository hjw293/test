import requests
import json

response = requests.get('http://localhost:8080/api/data', timeout=10)
data = response.json()
machine01_data = data.get('data', {}).get('machine01', [])

date_groups = {}
for item in machine01_data:
    date = item.get('date')
    if date not in date_groups:
        date_groups[date] = []
    date_groups[date].append(item)

print(f"machine01 total records: {len(machine01_data)}")
print(f"unique dates: {sorted(date_groups.keys())}")
print()

for date, items in sorted(date_groups.items()):
    avg_value = sum(item["value"] for item in items) / len(items)
    print(f"Date {date}: {len(items)} records, avg value: {avg_value:.2f}")
    print(f"  Sample timestamps: {sorted([item['timestamp'] for item in items[:3]])}")