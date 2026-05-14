# 详细分析页面加载慢的原因
Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient

Write-Host "========== 页面加载性能分析 ==========" -ForegroundColor Cyan

# 步骤1：刷新缓存
Write-Host "`n步骤1: 刷新缓存（清除旧缓存并从数据库重新加载）" -ForegroundColor Yellow
$refreshRequest = New-Object System.Net.Http.HttpRequestMessage
$refreshRequest.RequestUri = "http://localhost:8080/api/refresh-cache"
$refreshRequest.Method = [System.Net.Http.HttpMethod]::Post

$refreshStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$refreshResponse = $httpClient.SendAsync($refreshRequest).Result
$refreshStopwatch.Stop()

Write-Host "   耗时: $($refreshStopwatch.ElapsedMilliseconds) ms"
Write-Host "   状态: $($refreshResponse.StatusCode)"

# 步骤2：获取数据
Write-Host "`n步骤2: 从缓存获取数据（Gzip压缩）" -ForegroundColor Yellow
$getDataRequest = New-Object System.Net.Http.HttpRequestMessage
$getDataRequest.RequestUri = "http://localhost:8080/api/data"
$getDataRequest.Method = [System.Net.Http.HttpMethod]::Get
$getDataRequest.Headers.Add("Accept-Encoding", "gzip")

$getDataStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$getDataResponse = $httpClient.SendAsync($getDataRequest).Result
$getDataStopwatch.Stop()

$dataContent = $getDataResponse.Content.ReadAsByteArrayAsync().Result

Write-Host "   耗时: $($getDataStopwatch.ElapsedMilliseconds) ms"
Write-Host "   压缩后大小: $($dataContent.Length) bytes ($([math]::Round($dataContent.Length/1MB, 2)) MB)"
Write-Host "   Content-Encoding: $($getDataResponse.Content.Headers.ContentEncoding)"

# 总耗时
$totalTime = $refreshStopwatch.ElapsedMilliseconds + $getDataStopwatch.ElapsedMilliseconds

Write-Host "`n========== 性能分析结果 ==========" -ForegroundColor Green
Write-Host "   刷新缓存耗时: $($refreshStopwatch.ElapsedMilliseconds) ms"
Write-Host "   获取数据耗时: $($getDataStopwatch.ElapsedMilliseconds) ms"
Write-Host "   总耗时（后端）: $totalTime ms"

Write-Host "`n========== 瓶颈分析 ==========" -ForegroundColor Cyan

# 分析哪个步骤是瓶颈
if ($refreshStopwatch.ElapsedMilliseconds -gt 5000) {
    Write-Host "   主要瓶颈: 刷新缓存（数据库查询+Redis写入）" -ForegroundColor Red
    Write-Host "      - 原因: 数据量大（157,100条），数据库查询和Redis序列化耗时" -ForegroundColor Yellow
    Write-Host "      - 建议: 考虑减少刷新频率，使用更智能的缓存策略" -ForegroundColor Yellow
}
if ($getDataStopwatch.ElapsedMilliseconds -gt 5000) {
    Write-Host "   主要瓶颈: 获取数据（Redis读取+网络传输+压缩）" -ForegroundColor Red
    Write-Host "      - 原因: 数据量大，网络传输和解压耗时" -ForegroundColor Yellow
}
if ($refreshStopwatch.ElapsedMilliseconds -le 5000 -and $getDataStopwatch.ElapsedMilliseconds -le 5000) {
    Write-Host "   后端性能良好" -ForegroundColor Green
}

Write-Host "`n========== 数据量信息 ==========" -ForegroundColor Cyan
Write-Host "   数据总量: 157,100 条"
Write-Host "   压缩后: ~$([math]::Round($dataContent.Length/1MB, 2)) MB"
Write-Host "   压缩率: ~93%"

Write-Host "`n========== 优化建议 ==========" -ForegroundColor Cyan
Write-Host "   1. 分页加载: 不要一次性加载所有数据，使用分页接口" -ForegroundColor Yellow
Write-Host "   2. 减少数据: 只返回必要的字段，过滤不需要的数据" -ForegroundColor Yellow
Write-Host "   3. 虚拟滚动: 前端使用虚拟滚动技术，只渲染可见区域" -ForegroundColor Yellow
Write-Host "   4. 增量更新: 只请求新增的数据，而不是刷新所有数据" -ForegroundColor Yellow

$httpClient.Dispose()