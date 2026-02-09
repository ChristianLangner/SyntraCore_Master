# Autor: Christian Langner
# Projekt: AsyntraCore Setup & Repair

Write-Host "🚀 Starte AsyntraCore Super-Setup..." -ForegroundColor Cyan

# 1. Radikale Bereinigung
Write-Host "🧹 Lösche Cache und Binärdateien..." -ForegroundColor Yellow
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue bin/
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue target/
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue .kilocode/context/cache/

# 2. .kilocodeignore sicherstellen
Write-Host "🛡️ Konfiguriere .kilocodeignore gegen 422-Errors..." -ForegroundColor Yellow
$ignoreContent = @"
target/
bin/
*.class
.git/
.idea/
*.iml
*.log
.env
"@
$ignoreContent | Out-File -FilePath ".kilocodeignore" -Encoding utf8

# 3. Maven Wrapper fixen
Write-Host "🔧 Fixe Maven Wrapper Berechtigungen..." -ForegroundColor Yellow
Unblock-File -Path ./mvnw
if ($IsOptional -eq $false) { # Falls Linux/WSL im Hintergrund
    chmod +x mvnw 2>$null
}

# 4. VS Code Settings für Kilo Code optimieren
Write-Host "⚙️ Optimiere VS Code Workspace Settings..." -ForegroundColor Yellow
$settingsPath = ".vscode/settings.json"
if (!(Test-Path ".vscode")) { New-Item -ItemType Directory -Path ".vscode" }
$vscodeSettings = @{
    "java.configuration.updateBuildConfiguration" = "automatic"
    "java.import.maven.enabled" = $true
    "kilocode.context.exclude" = @("**/target/**", "**/bin/**", "**/*.class")
}
$vscodeSettings | ConvertTo-Json | Out-File -FilePath $settingsPath -Encoding utf8

# 5. Build-Check
Write-Host "🔨 Triggere ersten sauberen Build..." -ForegroundColor Cyan
./mvnw clean compile -DskipTests

Write-Host "✅ Setup abgeschlossen! Bitte Kilo Code Session jetzt neustarten." -ForegroundColor Green