Write-Host "Stopping frontend and backend services..."

# Find and terminate process using port 8080 (backend service)
Write-Host "Stopping backend service (port 8080)..."
try {
    $processes8080 = netstat -ano | Select-String ":8080" | Select-String "LISTENING"
    if ($processes8080) {
        foreach ($process in $processes8080) {
            $processId = $process.ToString().Split()[-1]
            Write-Host "Terminating process $processId"
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "No process found using port 8080"
    }
} catch {
    Write-Host "Error stopping backend service: $($_.Exception.Message)"
}

# Find and terminate process using port 5173 or 5174 (frontend service)
Write-Host "Stopping frontend service (ports 5173, 5174)..."
try {
    $frontendPorts = @("5173", "5174")
    foreach ($port in $frontendPorts) {
        $processes = netstat -ano | Select-String ":$port" | Select-String "LISTENING"
        if ($processes) {
            foreach ($process in $processes) {
                $processId = $process.ToString().Split()[-1]
                Write-Host "Terminating process $processId (port $port)"
                Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            }
        }
    }
} catch {
    Write-Host "Error stopping frontend service: $($_.Exception.Message)"
}

Write-Host "Both services have been stopped"
Write-Host "Press any key to close this window..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')