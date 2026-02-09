# Autor: Christian Langner
# Projekt: AyntraCore Initial Setup & Build Configuration

Write-Host "🚀 Starte AyntraCore Setup..." -ForegroundColor Cyan

# 1. Radikale Bereinigung
Write-Host "🧹 Lösche Cache und Binärdateien..." -ForegroundColor Yellow
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue bin/
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue target/
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue .idea/

# 2. Maven Wrapper Berechtigungen
Write-Host "🔧 Fixe Maven Wrapper Berechtigungen..." -ForegroundColor Yellow
Unblock-File -Path ./mvnw -ErrorAction SilentlyContinue

# 3. VS Code Workspace initialisieren
Write-Host "⚙️ Konfiguriere VS Code Workspace..." -ForegroundColor Yellow
if (!(Test-Path ".vscode")) { 
    New-Item -ItemType Directory -Path ".vscode" | Out-Null
}

# 4. Build-Check
Write-Host "🔨 Triggere ersten sauberen Build..." -ForegroundColor Cyan
./mvnw clean compile -DskipTests

Write-Host "✅ Setup abgeschlossen!" -ForegroundColor Green