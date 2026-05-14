# 测试Gzip压缩效果
Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient

Write-Host "Testing compression..." -ForegroundColor Cyan

# 测试1：不请求压缩
Write-Host "`n1. Without compression:" -ForegroundColor Yellow
$request1 = New-Object System.Net.Http.HttpRequestMessage
$request1.RequestUri = "http://localhost:8080/api/data"
$request1.Method = [System.Net.Http.HttpMethod]::Get
$request1.Headers.Add("Accept-Encoding", "identity")

$stopwatch1 = [System.Diagnostics.Stopwatch]::StartNew()
$response1 = $httpClient.SendAsync($request1).Result
$stopwatch1.Stop()

$content1 = $response1.Content.ReadAsByteArrayAsync().Result
Write-Host "   Status: $($response1.StatusCode)"
Write-Host "   Content-Length: $($content1.Length) bytes ($([math]::Round($content1.Length/1MB, 2)) MB)"
Write-Host "   Time: $($stopwatch1.ElapsedMilliseconds) ms"
Write-Host "   Content-Encoding: $($response1.Content.Headers.ContentEncoding)"

# 测试2：请求Gzip压缩
Write-Host "`n2. With gzip compression:" -ForegroundColor Yellow
$request2 = New-Object System.Net.Http.HttpRequestMessage
$request2.RequestUri = "http://localhost:8080/api/data"
$request2.Method = [System.Net.Http.HttpMethod]::Get
$request2.Headers.Add("Accept-Encoding", "gzip")

$stopwatch2 = [System.Diagnostics.Stopwatch]::StartNew()
$response2 = $httpClient.SendAsync($request2).Result
$stopwatch2.Stop()

$content2 = $response2.Content.ReadAsByteArrayAsync().Result
Write-Host "   Status: $($response2.StatusCode)"
Write-Host "   Content-Length: $($content2.Length) bytes ($([math]::Round($content2.Length/1MB, 2)) MB)"
Write-Host "   Time: $($stopwatch2.ElapsedMilliseconds) ms"
Write-Host "   Content-Encoding: $($response2.Content.Headers.ContentEncoding)"

# 计算压缩效果
if ($response2.Content.Headers.ContentEncoding -eq "gzip") {
    $compressionRatio = [math]::Round((1 - $content2.Length / $content1.Length) * 100, 1)
    $speedImprovement = [math]::Round((1 - $stopwatch2.ElapsedMilliseconds / $stopwatch1.ElapsedMilliseconds) * 100, 1)

    Write-Host "`nCompression Results:" -ForegroundColor Green
    Write-Host "   Compression ratio: $compressionRatio%"
    Write-Host "   Speed improvement: $speedImprovement%"
} else {
    Write-Host "`nWarning: Gzip compression is not enabled!" -ForegroundColor Red
}

$httpClient.Dispose()