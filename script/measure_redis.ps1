$uri='http://localhost:8080/api/data'
$out='measure_results.txt'
"Starting at $(Get-Date)" | Out-File $out -Encoding utf8
try {
  $c = New-Object System.Net.Sockets.TcpClient('127.0.0.1',6379)
  $s = $c.GetStream()
  $b = [System.Text.Encoding]::UTF8.GetBytes("*1`r`n`$8`r`nFLUSHALL`r`n")
  $s.Write($b,0,$b.Length)
  $s.Close(); $c.Close()
  "Sent FLUSHALL" | Out-File -Append $out -Encoding utf8
} catch {
  "Redis FLUSHALL failed: $_" | Out-File -Append $out -Encoding utf8
}

"-- Cold-cache tests (first 5 requests after FLUSHALL) --" | Out-File -Append $out -Encoding utf8
for ($i=1; $i -le 5; $i++) {
  try {
    $t = (Measure-Command { Invoke-RestMethod -Uri $uri -TimeoutSec 60 }).TotalMilliseconds
    ('cold#' + $i + ': ' + $t + ' ms') | Out-File -Append $out -Encoding utf8
  } catch { ('cold#' + $i + ': request failed: ' + $_.ToString()) | Out-File -Append $out -Encoding utf8 }
}

"-- Warm-cache tests (next 10 requests) --" | Out-File -Append $out -Encoding utf8
for ($i=1; $i -le 10; $i++) {
  try {
    $t = (Measure-Command { Invoke-RestMethod -Uri $uri -TimeoutSec 60 }).TotalMilliseconds
    ('warm#' + $i + ': ' + $t + ' ms') | Out-File -Append $out -Encoding utf8
  } catch { ('warm#' + $i + ': request failed: ' + $_.ToString()) | Out-File -Append $out -Encoding utf8 }
}

"Finished at $(Get-Date)" | Out-File -Append $out -Encoding utf8
