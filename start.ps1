$ErrorActionPreference = "SilentlyContinue"

$BACKEND_PORT = "8080"
$FRONTEND_PORT = "5173"
$PROJECT_ROOT = "d:\Study\Fongs\fongs"

Write-Host "======================================="
Write-Host "  Sensor Project - Start Script"
Write-Host "======================================="

# Stop existing services
Write-Host "`n[1/3] Stopping existing services..."
$ports = @($BACKEND_PORT, $FRONTEND_PORT)
foreach ($port in $ports) {
    $proc = netstat -ano | Select-String ":$port\s" | Select-String "LISTENING" | Select-Object -First 1
    if ($proc) {
        $pid = $proc.ToString().Split()[-1]
        Stop-Process -Id $pid -Force
        Write-Host "  Port $port (PID $pid) stopped"
    } else {
        Write-Host "  Port $port - no process found"
    }
}
Start-Sleep -Seconds 1

# Start backend
Write-Host "`n[2/3] Starting backend (port $BACKEND_PORT)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PROJECT_ROOT\backend'; mvn spring-boot:run" -WindowStyle Normal

Write-Host "  Backend starting in new window..."

# Wait for backend to initialize
Write-Host "`n  Waiting 8 seconds for backend..."
Start-Sleep -Seconds 8

# Start frontend
Write-Host "`n[3/3] Starting frontend (port $FRONTEND_PORT)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PROJECT_ROOT\frontend'; npm run dev" -WindowStyle Normal

Write-Host "  Frontend starting in new window..."

Start-Sleep -Seconds 3

Write-Host "`n======================================="
Write-Host "  Services started:"
Write-Host "  - Backend: http://localhost:$BACKEND_PORT"
Write-Host "  - Frontend: http://localhost:$FRONTEND_PORT"
Write-Host "======================================="