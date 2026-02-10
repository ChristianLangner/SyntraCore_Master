# API_Contracts_RAG_and_Image.md

## RAG
- **GET** `/api/insights/rag?companyId={uuid}&since={iso}` -> Aggregierte Metriken
- **POST** `/api/agent/entry` (mode=text) -> Antwort + Quellen + uiHints

## Image
- **PATCH** `/api/persona/{id}/traits` -> `image.{seed,modelId,sampler,steps,cfgScale,aspect}`
- **POST** `/api/image/generate` -> `{url, meta}` (Adapter-intern auch über AgentEntry möglich)

## Persona
- **POST** `/api/persona/switch` -> aktive Persona + uiHints

## Sicherheit
- Jeder Call erfordert `companyId` (Header oder Query); bei Fehlen **403**.