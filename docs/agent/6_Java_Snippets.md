
# Java Snippets (Referenz)

```java
// Record-DTO (Java 21)
public record KnowledgeHit(String id, String category, double score, String snippet) {}

// Lombok in Adaptern
@Data
@Builder
@RequiredArgsConstructor
public class VectorSearchResult {
  private final List<KnowledgeHit> matches;
  private final long embeddingMs;
  private final long dbQueryMs;
}

// Hinweis: Für JSONB-Mapping (persona.traits) hypersistence-utils verwenden.
```
