
# README | Verwendung der MD-Anhänge in Google IDX

## Zweck
Dieses Paket liefert getrennte Markdown-Dateien, die du als **Anhang** in Gesprächen/Anweisungen an deinen Agenten in **Google IDX** hinzufügen kannst.

## Dateien
1. **1_SystemPrompt_Gemini_IDX.md** – Vollständiger Systemprompt für Gemini.
2. **2_Developer_Instructions.md** – Richtlinien für Tools, Logging, Antwortstruktur.
3. **3_Tool_Schemas.json.md** – Function-Definitionen inkl. Beispiele.
4. **4_Agent_Antwortvorlage.md** – Template, das der Agent beim Antworten nutzt.
5. **5_Orchestrierung_Pseudocode.md** – End-to-end Call-Flow (Pseudocode).
6. **6_Java_Snippets.md** – Referenz für DTOs und Ergebnisse.
7. **7_E2E_Testprompts.md** – Prompts zur Validierung wichtiger Pfade.

## Nutzung in IDX
- Importiere eine oder mehrere dieser Dateien als **Attachment** oder als **Kontext-Dateien**.
- Verweise in deiner Anweisung auf die jeweilige Datei (z. B. „nutze *1_SystemPrompt_Gemini_IDX.md* als Systemrichtlinie“).
- Für Tool-Calls stelle sicher, dass die IDX-Tooling-Schnittstellen die hier dokumentierten Felder (insb. `companyId`) unterstützen.

## Tipps
- Halte die **Systemprompt-Datei** schlank; Details in Developer/Tool-Schemas auslagern.
- Versioniere diese MD-Dateien in Git, um Änderungen nachvollziehbar zu machen.
