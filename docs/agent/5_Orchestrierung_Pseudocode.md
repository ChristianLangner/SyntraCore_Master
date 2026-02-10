
# Orchestrierung (Pseudocode)

```pseudo
require companyId
embedding = tools.embeddings_create({ companyId, text: user.query })
res = tools.vector_search({ companyId, embedding, topK: 8, minScore: 0.60 })

log [LATENCY] embedding_ms:res.latency.embedding_ms db_query_ms:res.latency.db_query_ms total_ms:calc()

for m in res.matches:
  log [VECTOR_MATCH] ID:m.id Similarity:m.score Category:m.category
  if m.score < 0.70: log [LOW_RELEVANCE_WARNING] ID:m.id Similarity:m.score

good = filter(res.matches, m => m.score >= 0.70)
if size(good) == 0:
  respond("Keine ausreichend relevanten Treffer. Vorschlag: Kategorie oder Suchbegriff präzisieren.")
else:
  ctx = summarize(good.snippets)
  respond(markdown_answer(ctx, sources=good))
  ui_hints({ primaryColor: persona.traits.themeColor, streaming: true, showSourceCategories: true })
```
