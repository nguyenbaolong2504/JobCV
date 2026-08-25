param(
    [switch]$Rebuild
)

$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:CATALINA_HOME = "C:\apache-tomcat-9.0.120"
$env:CATALINA_BASE = $env:CATALINA_HOME
$env:RECRUITFLOW_DB_URL = "jdbc:mysql://localhost:3306/recruitflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&useUnicode=true&characterEncoding=UTF-8"
$env:RECRUITFLOW_DB_USER = "root"

$existingServer = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1
if ($existingServer) {
    $process = Get-Process -Id $existingServer.OwningProcess -ErrorAction SilentlyContinue
    $processName = if ($process) { $process.ProcessName } else { "khong ro" }
    Write-Host "Cong 8080 dang duoc su dung boi PID $($existingServer.OwningProcess) ($processName)." -ForegroundColor Yellow
    $recruitFlowHealthy = $false
    try {
        $healthResponse = Invoke-WebRequest "http://localhost:8080/recruitflow/home" -UseBasicParsing -TimeoutSec 2
        $recruitFlowHealthy = $healthResponse.StatusCode -eq 200
    } catch {
        $recruitFlowHealthy = $false
    }
    if ($recruitFlowHealthy) {
        Write-Host "RecruitFlow dang chay tai http://localhost:8080/recruitflow/home, khong can chay lai." -ForegroundColor Green
    } else {
        Write-Host "Tien trinh nay khong phuc vu RecruitFlow. Hay dung dung server dang chiem cong roi chay lai lenh." -ForegroundColor Red
        Write-Host "Lenh dung: Stop-Process -Id $($existingServer.OwningProcess) -Force" -ForegroundColor Yellow
    }
    exit 1
}

$dbSecret = Read-Host "Nhap mat khau MySQL" -AsSecureString
$env:RECRUITFLOW_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $dbSecret).Password

Push-Location $PSScriptRoot
try {
    $deployedWar = Join-Path $env:CATALINA_HOME "webapps\recruitflow.war"
    if ($Rebuild -or -not (Test-Path -LiteralPath $deployedWar)) {
        & mvn -s ".\.mvn\codex-settings.xml" package
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build that bai."
        }
        Copy-Item ".\target\recruitflow-1.0-SNAPSHOT.war" $deployedWar -Force
    } else {
        Write-Host "Dang dung ban RecruitFlow da trien khai. Them -Rebuild neu muon build lai ma nguon." -ForegroundColor Cyan
    }
    & "$env:CATALINA_HOME\bin\catalina.bat" run
} finally {
    Pop-Location
}
