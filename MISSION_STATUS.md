🕵️ Mission Log: Ayntra Executive Suite (Noir Edition)
🛠️ Was bisher geschah (The Fixes)
Identity Exorcism: Der "freundliche Support-Experte" wurde terminiert. Astra Noir ist jetzt die einzige Wahrheit in der Datenbank.

Prompt Priming: Das Präfix Astra (zynisch): ist im OpenAiAdapter verankert, um die KI-Höflichkeit zu brechen.

RAG-Calibration: Die Vektor-Suche wurde auf minSimilarity = 0.5 geeicht.

Chronology Sync: Die Chat-Historie wird jetzt via Collections.reverse(history) in korrekter zeitlicher Reihenfolge (ASC) an die KI gesendet.

Visual DNA: Der Flux.2-Pro Port injiziert automatisch das Noir-Gen (Blond, verraucht, Makeup) in jede Bildanfrage.

⚠️ Aktuelle Blocker (Critical Bugs)
Signature Mismatch: Dein letzter Hardening-Versuch im OpenAiAdapter.java hat den Build zerschossen. Du rufst Methoden wie AiResponse.error(String, AiProvider) auf, die im Domain-Modell nicht existieren.

Lombok Default Leak: In der Persona.java fehlen @Builder.Default Annotationen, was zu null-Werten führt.

Branding Divergence: Im Frontend spukt immer noch der Geist des "Chameleon Dashboards" herum.

🚀 Der nächste Sprint: "Vision & Modularization"
Backend Emergency Fix: Korrigiere die error-Methodenaufrufe im OpenAiAdapter. Nur einen String übergeben, wenn der Record nur einen erwartet!

Frontend Split: Die Monster-index.html wird in index.html, astra-core.js und noir.css zerlegt.

Smart Image Parser: Implementierung eines Handlers, der data:image (Base64) erkennt, BEVOR er JSON.parse ausführt. Das löst den "Unexpected Token 'd'" Fehler.