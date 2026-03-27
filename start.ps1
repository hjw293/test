# Show current directory
Write-Host "Current directory: $pwd"

# Start backend service
Write-Host "Starting backend service..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d d:\Study\test\backend && echo Backend directory: %cd% && mvn spring-boot:run" -WindowStyle Normal -WorkingDirectory "d:\Study\test\backend"

# Wait for 3 seconds to ensure backend service has enough time to start
Write-Host "Waiting for backend service to start..."
Start-Sleep -Seconds 3

# Start frontend service
Write-Host "Starting frontend service..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d d:\Study\test\frontend && echo Frontend directory: %cd% && npm run dev" -WindowStyle Normal -WorkingDirectory "d:\Study\test\frontend"

Write-Host "Both services have been started. Please access the frontend at http://localhost:5173"
Write-Host "Press any key to close this window..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')