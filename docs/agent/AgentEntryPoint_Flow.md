# AgentEntryPoint_Flow.md

## Ziel
Einheitlicher Entry-Point für Text/RAG und Image-Generierung, mit Multi-Tenancy-Gate und Quellen-Transparenz.

## Pseudocode
```pseudo
POST /api/agent/entry (companyId, personaId, mode, query, options)

require companyId
persona = PersonaService.load(personaId, companyId)
uiHints = persona.uiHints

if mode == "text":
  emb = Embeddings.create(query)
  matches, latency = Vector.search(companyId, emb, topK=options.topK, minScore=options.minScore)
  log [EMBEDDING_MS] ..., [DB_MS] ..., [TOTAL_MS] ...
  good = filter(matches, score>=0.70)
  if good.empty: return {answer:"Keine ausreichend relevanten Treffer...", sources:[], uiHints}
  ctx = Summarize(good)
  answer = LLM.generate(ctx, query)
  return {answer, sources:good, uiHints}

if mode == "image":
  meta = persona.traits.image
  params = parseImageParams(query/options)
  callArgs = merge(meta, params)  // seedLock berücksichtigen
  img = ImageAdapter.generate(callArgs)
  return {answer: img.url, sources:[], uiHints}
```

## Fehlerbehandlung
- 400: invalid mode/params
- 403: companyId fehlt
- 5xx: Providerfehler -> Retry/Backoff, Logging
```