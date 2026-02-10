
# Tool Schemas (Function Definitions)

> **Hinweis:** Benenne die Funktionen in IDX passend zu deinem Stack.

```json
{
  "name": "embeddings_create",
  "description": "Erzeugt Embeddings für einen Text.",
  "parameters": {
    "type": "object",
    "properties": {
      "companyId": { "type": "string", "description": "UUID des Tenants" },
      "text": { "type": "string" }
    },
    "required": ["companyId", "text"]
  }
}
```

```json
{
  "name": "vector_search",
  "description": "Sucht relevante Dokumente via pgvector (Cosine).",
  "parameters": {
    "type": "object",
    "properties": {
      "companyId": { "type": "string" },
      "embedding": { "type": "array", "items": { "type": "number" } },
      "topK": { "type": "integer", "default": 8 },
      "minScore": { "type": "number", "default": 0.60 }
    },
    "required": ["companyId", "embedding"]
  }
}
```

```json
{
  "name": "doc_fetch",
  "description": "Holt Volltext + Metadaten einzelner IDs.",
  "parameters": {
    "type": "object",
    "properties": {
      "companyId": { "type": "string" },
      "ids": { "type": "array", "items": { "type": "string" } }
    },
    "required": ["companyId", "ids"]
  }
}
```

**Erwartete Tool-Response (Beispiel)**

```json
{
  "matches": [
    { "id": "kb_421", "category": "Policies", "score": 0.81, "snippet": "..." },
    { "id": "kb_122", "category": "API", "score": 0.73, "snippet": "..." }
  ],
  "latency": { "embedding_ms": 15, "db_query_ms": 42 }
}
```
