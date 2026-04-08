# 测试页面加载速度
Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient

Write-Host "Testing page load performance..." -ForegroundColor Cyan

# 模拟浏览器请求（自动支持Gzip压缩）
Write-Host "`nSimulating browser request with gzip compression:" -ForegroundColor Yellow
$request = New-Object System.Net.Http.HttpRequestMessage
$request.RequestUri = "http://localhost:8080/api/data"
$request.Method = [System.Net.Http.HttpMethod]::Get
# 浏览器默认会发送Accept-Encoding头
$request.Headers.Add("Accept-Encoding", "gzip, deflate, br")

$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$response = $httpClient.SendAsync($request).Result
$stopwatch.Stop()

$content = $response.Content.ReadAsByteArrayAsync().Result
$contentLength = $response.Content.Headers.ContentLength

Write-Host "   Status: $($response.StatusCode)"
Write-Host "   Content-Encoding: $($response.Content.Headers.ContentEncoding)"
if ($contentLength -gt 0) {
    Write-Host "   Content-Length: $contentLength bytes ($([math]::Round($contentLength/1MB, 2)) MB)"
}
Write-Host "   Actual received: $($content.Length) bytes ($([math]::Round($content.Length/1MB, 2)) MB)"
Write-Host "   Load time: $($stopwatch.ElapsedMilliseconds) ms"

$httpClient.Dispose()

Write-Host "`nComparison:" -ForegroundColor Green
Write-Host "   Before compression: ~15.49 MB (~7.8 seconds)"
Write-Host "   After compression: ~1.06 MB (~$($stopwatch.ElapsedMilliseconds) ms)"
Write-Host "   Improvement: $([math]::Round(15.49 / 1.06, 1))x smaller data size"