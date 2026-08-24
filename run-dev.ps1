param(
    [switch]$Rebuild
)

$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
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
    Write-Host "Neu RecruitFlow da mo tai http://localhost:8080/recruitflow/home thi khong can chay lai." -ForegroundColor Yellow
    Write-Host "Muon khoi dong lai, dung server cu bang Ctrl+C hoac Stop-Process -Id $($existingServer.OwningProcess) -Force." -ForegroundColor Yellow
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
