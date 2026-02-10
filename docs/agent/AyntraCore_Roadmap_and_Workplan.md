
# AyntraCore | Roadmap & Workplan (Agent-ready)

**Version:** 1.0  
**Datum:** 10.02.2026  
**Bezug:**
- `AyntraCore_Chameleon_Dashboard_Frontend_Spec.md`
- `AyntraCore_Image_Generation_Bridge_Spec.md`
- `AyntraCore_ImageGen_End2End.md`
- `1_SystemPrompt_Gemini_IDX.md` · `2_Developer_Instructions.md` · `3_Tool_Schemas.json.md`

---

## 0) Zielbild
Ein produktionsreifes **Chameleon Dashboard** mit stabiler RAG-Kette, konsistenter Bildgenerierung, Multi-Tenancy-Sicherheit, Observability und CI/CD – inklusive klarer **Agent-Flows** und **DoD-Kriterien**.

---

## 1) Phasen & Meilensteine (KW-basiert)

| Phase | Zeitraum | Fokus | Meilenstein (Exit) |
|------:|:--------:|-------|--------------------|
| P0 | KW 7–9 | Stabilisierung Backend/RAG | VECTOR_MATCH/LATENCY sauber, MT-Gate aktiv (403 bei fehlender companyId) |
| P1 | KW 10–13 | Persona v2 + Image Sidebar Final | PersonaMatrix aktiv, Sidebar: Sampler/Steps/AR + Persistenz |
| P2 | KW 14–17 | AgentEntryPoint + Realtime RAG | Einheitlicher Flow live, WS-Streaming für Insights |
| P3 | KW 18–20 | KB-Dashboard + Tests | KB-Admin, Relevanz-Tester, Regression-Suite grün |
| P4 | KW 21–24 | Prod Hardening | SLAs/Runbooks, p95 < 800 ms, On-call |

---

## 2) Workstreams (detailliert)

### 2.1 Backend Stabilisierung & Observability (P0)
**Ziel:** Robustheit und Messbarkeit der RAG-Pfad-Latenzen und Scores.
- [ ] **RAGInsightsService** einführen (Aggregation von VECTOR_MATCH, LOW_RELEVANCE_WARNING, Latenzen)
- [ ] Endpunkt: `GET /api/insights/rag?companyId=...&since=...` (JSON: scores, categories, p95-ms)
- [ ] Logging: getrennte Marker `[EMBEDDING_MS]`, `[DB_MS]`, `[TOTAL_MS]`
- [ ] **Tenant Gate**: Globaler Filter (403 wenn `companyId` fehlt)
**DoD:** p95 total_ms pro Query < 1.0 s (Staging), 100% Endpunkte erzwingen companyId.

### 2.2 Persona System v2 (P1)
**Ziel:** Konsistente App-Szenarien als Presets + Live-Wechsel.
- [ ] `persona_matrix.json` laden und in `PersonaService` mergen
- [ ] API: `POST /api/persona/switch` -> setzt aktive Persona, liefert `uiHints` + `traits`
- [ ] Frontend konsumiert `uiHints` und validiert (Theming/Streaming/Transparenz)
**DoD:** Persona-Wechsel ohne Reload, UI-Hints = OK, Traits konsistent.

### 2.3 Image Sidebar Finalization (P1)
**Ziel:** Sampler/Steps/Aspect-Ratio vollständig, persistiert und verwendet.
- [ ] UI Felder: `sampler (select)`, `steps (range)`, `aspectRatio (radio)`
- [ ] PATCH `/api/persona/:id/traits` -> `image.sampler`, `image.steps`, `image.aspect`
- [ ] Adapter: `ImageCallArgs` ergänzt `sampler`, `steps`, optional `aspect`
**DoD:** Gleiches Prompt + SeedLock=true = visuell konsistent (3 Läufe), Presets ändern CFG/Model/AR.

### 2.4 Realtime RAG-Insights (P2)
**Ziel:** Live-Feedback zu Retrieval-Qualität.
- [ ] WS-Kanal `/ws/insights` -> push: {{ matches:[{{id,category,score}}], latency }}
- [ ] Frontend: Live-Panel, Score-Bar, Kategorie-Badges, Latenz-Badges
**DoD:** Unter 200 ms UI-Update nach Backend-Event (Staging), graceful fallback bei WS-Abbruch.

### 2.5 AgentEntryPoint (P2)
**Ziel:** Einheitlicher Endpunkt für alle Agenten-Aufgaben.
- [ ] Controller: `POST /api/agent/entry` (body: companyId, personaId, query, mode=image|text, options)
- [ ] Flow siehe `AgentEntryPoint_Flow.md` (Preflight -> Merge -> Call -> UI-Hints)
**DoD:** 100% Pfade nutzen companyId, Quellen-Transparenz garantiert, Fehler sauber (4xx/5xx) modelliert.

### 2.6 Knowledge Base Dashboard (P3)
**Ziel:** Admin-Übersicht, Relevanztester, Embedding-Wartung.
- [ ] UI: Kategorienliste, Filter je Tenant, Status (chunks/updatedAt)
- [ ] Action: "Re-Embed" pro Dokument/Kategorie
- [ ] Relevanz-Tester (Query -> Matches/Score/Latency + Export)
**DoD:** Bedienbar für Nicht-Entwickler, Export CSV/PDF verfügbar.

### 2.7 Production Hardening (P4)
**Ziel:** SLOs/SLAs, On-call, Runbooks.
- [ ] p95 total_ms < 800 ms, Error-Rate < 0.5%
- [ ] Runbooks: Incident/Degradation/Rollback
- [ ] Canaries + Feature Flags
**DoD:** Abnahme durch Eng.-Lead & Security-Lead, Monitoring-Dashboards live.

---

## 3) API Contracts (konkret)

### 3.1 RAG Insights
```http
GET /api/insights/rag?companyId={uuid}&since=2026-02-01T00:00:00Z
200 OK
{{
  "p95_total_ms": 740,
  "matches": [{{"id":"kb_421","category":"Policies","score":0.81}}]
}}
```

### 3.2 Persona Switch
```http
POST /api/persona/switch
Content-Type: application/json
{{
  "companyId":"...",
  "personaId":"..."
}}
200 OK
{{
  "persona": {{"id":"...","traits":{{}}}},
  "uiHints": {{"theming":{{"primaryColor":"#0EA5E9"}},"streaming":{{"enabled":true}},"transparency":{{"showSourceCategories":true}}}}
}}
```

### 3.3 Traits Patch (Image)
```http
PATCH /api/persona/{id}/traits
Content-Type: application/json
{{
  "image": {{"seed":1234, "modelId":"civitai/...","sampler":"DPM++ 2M Karras","steps":28,"cfgScale":6.5,"aspect":"square"}}
}}
200 OK
```

### 3.4 Agent Entry
```http
POST /api/agent/entry
Content-Type: application/json
{{
  "companyId":"...",
  "personaId":"...",
  "mode":"text|image",
  "query":"...",
  "options": {{"topK":8,"minScore":0.7}}
}}
200 OK
{{
  "answer":"...",
  "sources":[{{"id":"kb_421","category":"Policies","score":0.81}}],
  "uiHints":{{}}
}}
```

---

## 4) Copilot-Tasks (Copy & Paste)

### 4.1 Persona v2
```md
# TASK: Persona v2 Integration
- Lade persona_matrix.json in PersonaService.
- Implementiere POST /api/persona/switch (companyId, personaId) -> returns persona+uiHints.
- Frontend: Konsumiere uiHints, rufe applyPersonaBranding und prüfe Hints (OK/WARN).
- Tests: Persona-Wechsel ohne Reload, Traits gemerged, Hints=OK.
```

### 4.2 Image Sidebar Final
```md
# TASK: Image Sidebar Finalization
- Füge Sampler/Steps/Aspect Ratio (square|portrait|landscape) in admin.html hinzu.
- Persistiere Werte in traits.image via PATCH.
- Erweitere ImageCallArgs um sampler/steps/aspect.
- Tests: SeedLock=true -> konsistente Bilder; Presets setzen CFG/Model/AR.
```

### 4.3 Realtime RAG-Insights
```md
# TASK: Realtime RAG Insights
- Implementiere WS /ws/insights (server push matches/latency). 
- Frontend: Live-Panel aktualisiert Scores/Kategorien in <200ms.
- Fallback: Polling GET /api/insights/rag.
```

### 4.4 Agent Entry Point
```md
# TASK: AgentEntryPoint
- POST /api/agent/entry implementieren (companyId-Gate, Persona-Load, RAG, Answer, uiHints).
- Fehlerfälle: 400 (invalid), 403 (no companyId), 5xx (Provider).
- Tests: Quellenliste + Threshold-Warnungen korrekt.
```

---

## 5) Definition of Done (global)
- **Security:** 100% Endpunkte erfordern `companyId`.
- **Transparenz:** Quellenliste + Kategorien in jeder Antwort.
- **Latenz:** p95 total_ms < 1.0 s (Staging), < 0.8 s (Prod).
- **Logging:** VECTOR_MATCH, LOW_RELEVANCE_WARNING, EMBEDDING_MS, DB_MS, TOTAL_MS, IMAGE_CALL, LATENCY.
- **Tests:** Unit (Persona/RAG/Image), Integration (AgentEntry), E2E (SeedLock/Presets/KB-Relevanz).

---

## 6) Abhängigkeiten & Risiken
- **Provider Limits (Image/LLM)** -> Rate-Limit + Retry-Policy.
- **KB-Veraltung** -> Re-Embed Workflows + Staleness-Check.
- **Websocket-Abbrüche** -> Fallback Polling.

---

## 7) Artefakte
- `persona_matrix.json` (Presets je Szenario)
- `AgentEntryPoint_Flow.md` (Pseudocode & Fehlerbehandlung)
- `API_Contracts_RAG_and_Image.md` (konkretisierte Endpunkte)
