$rulesPath = ".kilocode/rules"
if (Test-Path $rulesPath) { Remove-Item -Recurse -Force $rulesPath }
New-Item -ItemType Directory -Force -Path $rulesPath

$globalRules = @"
# Rules
- Style: ONLY CODE. No text. No explanations.
- Comments: NO comments allowed in code blocks.
- Format: Raw code block output only.
"@

$globalRules | Out-File -FilePath "$rulesPath/global.md" -Encoding utf8

$settingsPath = ".vscode"
if (!(Test-Path $settingsPath)) { New-Item -ItemType Directory -Force -Path $settingsPath }

$vscodeSettings = @"
{
  "kilocode.openrouter.defaultModel": "google/gemini-2.0-flash-lite-preview-02-05:free",
  "kilocode.contextLimit": 2000,
  "kilocode.rules.directory": ".kilocode/rules"
}
"@

$vscodeSettings | Out-File -FilePath "$settingsPath/settings.json" -Encoding utf8

Write-Host "Kilo Code auf Diät gesetzt. Starte VS Code neu." -ForegroundColor Cyan