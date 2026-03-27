Write-Host "Stopping frontend and backend services..."

# Find and terminate process using port 8080 (backend service)
Write-Host "Stopping backend service (port 8080)..."
try {
    $processes8080 = netstat -ano | Select-String ":8080" | Select-String "LISTENING"
    if ($processes8080) {
        foreach ($process in $processes8080) {
            $pid = $process.ToString().Split()[-1]
            Write-Host "Terminating process $pid"
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "No process found using port 8080"
    }
} catch {
    Write-Host "Error stopping backend service: $($_.Exception.Message)"
}

# Find and terminate process using port 5173 (frontend service)
Write-Host "Stopping frontend service (port 5173)..."
try {
    $processes5173 = netstat -ano | Select-String ":5173" | Select-String "LISTENING"
    if ($processes5173) {
        foreach ($process in $processes5173) {
            $pid = $process.ToString().Split()[-1]
            Write-Host "Terminating process $pid"
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "No process found using port 5173"
    }
} catch {
    Write-Host "Error stopping frontend service: $($_.Exception.Message)"
}

Write-Host "Both services have been stopped"
Write-Host "Press any key to close this window..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')