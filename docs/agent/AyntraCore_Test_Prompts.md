# AyntraCore | Test Prompts & E2E Checks (2026-02-10)

> Diese Sammlung deckt Persona‑Wechsel, RAG‑Relevanz, AgentEntryPoint, Image‑Seed‑Lock, Realtime‑Insights und Security ab.

---

## 1) Persona Switch (API)
**Ziel:** Persona v2 & uiHints korrekt.

```bash
curl -sS -X POST http://localhost/api/persona/switch \
  -H 'Content-Type: application/json' \
  -d '{"companyId":"<TENANT_UUID>","personaId":"IHK"}' | jq
```
☑ **Erwartet:** uiHints.theming.primaryColor vorhanden, streaming.enabled=true, transparency.showSourceCategories=true.

---

## 2) Agent Entry – Text (RAG)
**Ziel:** Quellenliste + Threshold‑Warnungen.

```bash
curl -sS -X POST http://localhost/api/agent/entry \
  -H 'Content-Type: application/json' \
  -d '{
    "companyId":"<TENANT_UUID>",
    "personaId":"IHK",
    "mode":"text",
    "query":"Erkläre die Threshold-Logik und gib die Kategorien der Quellen an.",
    "options": {"topK":8, "minScore":0.70}
  }' | jq
```
☑ **Erwartet:** Antwort mit sources[] (id, category, score), ggf. LOW_RELEVANCE_WARNING < 0.70.

---

## 3) Agent Entry – Image (Seed-Lock)
**Ziel:** Konsistente Bilder mit festem Seed.

```bash
curl -sS -X POST http://localhost/api/agent/entry \
  -H 'Content-Type: application/json' \
  -d '{
    "companyId":"<TENANT_UUID>",
    "personaId":"RPG",
    "mode":"image",
    "query":"prompt=portrait of a ranger in a crystal cavern; seedLock=true",
    "options":{}
  }' | jq
```
☑ **Erwartet:** Gleiche URL/Look bei drei identischen Aufrufen (SeedLock=true).

---

## 4) RAG Insights – REST Polling

```bash
curl -sS "http://localhost/api/insights/rag?companyId=<TENANT_UUID>&since=2026-02-01T00:00:00Z" | jq
```
☑ **Erwartet:** p95_total_ms und matches[] mit Kategorien.

---

## 5) Realtime WS – Insights
**Ziel:** Live‑Panel < 200 ms Update.

1. Öffne ui/ws_client.html im Browser.
2. Triggere eine RAG‑Anfrage im System.
☑ **Erwartet:** WS‑Nachricht mit matches[] und Latenzen.

---

## 6) Knowledge Base – Re‑Embed
**Ziel:** Aktualisierte Embeddings.

```bash
curl -sS -X POST http://localhost/api/kb/reembed \
  -H 'Content-Type: application/json' \
  -d '{"companyId":"<TENANT_UUID>","category":"API"}' | jq
```
☑ **Erwartet:** Job‑Status, erneuerte updatedAt Werte.

---

## 7) Security – Negative Tests
Ohne companyId:

```bash
curl -sS -X POST http://localhost/api/agent/entry -H 'Content-Type: application/json' -d '{"mode":"text","query":"hi"}' -i
```
☑ **Erwartet:** 403 Forbidden.

Cross‑Tenant Zugriff: Dokument einer anderen companyId anfordern → 403.

---

## 8) UI – Image Sidebar (manuell)
1. Sidebar öffnen (Bild anklicken), Preset Photorealistic wählen.
2. Seed-Lock schließen (🔒), drei Mal Generieren.
3. Dann Seed-Lock öffnen (🔓) und erneut generieren.
☑ **Erwartet:** Mit 🔒 konsistent; mit 🔓 Variation.

---

## 9) Regression – Threshold Tuning
Ziel: Scores > 0.70 dominieren. Query: Welche Metriken loggen wir im Vector Adapter?
