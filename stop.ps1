$ErrorActionPreference = "SilentlyContinue"

$BACKEND_PORT = "8080"
$FRONTEND_PORT = "5173"

Write-Host "======================================="
Write-Host "  Sensor Project - Stop Script"
Write-Host "======================================="

Write-Host "`nStopping services..."

$ports = @($BACKEND_PORT, $FRONTEND_PORT)
foreach ($port in $ports) {
    $proc = netstat -ano | Select-String ":$port\s" | Select-String "LISTENING" | Select-Object -First 1
    if ($proc) {
        $pid = $proc.ToString().Split()[-1]
        Stop-Process -Id $pid -Force
        Write-Host "  Port $port (PID $pid) stopped"
    } else {
        Write-Host "  Port $port - not in use"
    }
}

Write-Host "`nDone."