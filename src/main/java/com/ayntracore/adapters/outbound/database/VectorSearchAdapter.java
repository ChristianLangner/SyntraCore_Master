// Autor: Christian Langner
package com.ayntracore.adapters.outbound.database;

import com.pgvector.PGvector;
import com.ayntracore.core.domain.Knowledge;
import com.ayntracore.core.ports.VectorSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Outbound Adapter für Vektor-basierte Ähnlichkeitssuche mit pgvector.
 *
 * <p><strong>Architektur-Schicht:</strong> Infrastructure Layer (Outbound Adapter)</p>
 * <p><strong>Hexagonale Architektur:</strong> Implementiert VectorSearchPort</p>
 *
 * <h2>Technologie-Stack:</h2>
 * <ul>
 *   <li><strong>Database:</strong> Neon PostgreSQL mit pgvector Extension</li>
 *   <li><strong>Embedding:</strong> OpenAI text-embedding-3-small (1536 Dimensionen)</li>
 *   <li><strong>Similarity:</strong> Cosine Similarity (operator: &lt;=&gt;)</li>
 *   <li><strong>JDBC:</strong> JdbcTemplate für native SQL-Abfragen</li>
 * </ul>
 *
 * <h2>Vektor-Operatoren (pgvector):</h2>
 * <ul>
 *   <li><strong>&lt;-&gt;</strong> L2 Distance (Euclidean)</li>
 *   <li><strong>&lt;=&gt;</strong> Cosine Distance (verwendet in diesem Adapter)</li>
 *   <li><strong>&lt;#&gt;</strong> Inner Product</li>
 * </ul>
 *
 * <h2>Performance-Optimierungen:</h2>
 * <ul>
 *   <li><strong>HNSW Index:</strong> Für schnelle Approximate Nearest Neighbor Search</li>
 *   <li><strong>Company-Filter:</strong> Pre-Filtering für Multi-Tenancy</li>
 *   <li><strong>Limit-Clause:</strong> Top-K Retrieval für effizienten RAG</li>
 * </ul>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see VectorSearchPort
 * @see Knowledge
 */
@Component
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class VectorSearchAdapter implements VectorSearchPort {

    private final JdbcTemplate jdbcTemplate;
    private final SpringDataKnowledgeRepository knowledgeRepository;
    private final RestClient.Builder restClientBuilder;

    private static final String OPENAI_EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private static final String OPENAI_EMBEDDING_MODEL = "text-embedding-3-small";

    /**
     * SQL-Query für Vektor-Ähnlichkeitssuche mit Cosine Distance.
     * Verwendet pgvector operator: <=> (Cosine Distance)
     *
     * 1 - (embedding <=> ?) = Cosine Similarity (0.0 - 1.0)
     */
    private static final String VECTOR_SEARCH_SQL = """
            SELECT id, company_id, content, category, source, embedding,
                   1 - (embedding <=> ?::vector) AS similarity
            FROM knowledge_base
            WHERE company_id = ?::uuid
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """;

    /**
     * SQL-Query mit Similarity-Threshold.
     */
    private static final String VECTOR_SEARCH_WITH_SCORE_SQL = """
            SELECT id, company_id, content, category, source, embedding,
                   1 - (embedding <=> ?::vector) AS similarity
            FROM knowledge_base
            WHERE company_id = ?::uuid
              AND 1 - (embedding <=> ?::vector) >= ?
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """;

    @Override
    public List<Knowledge> findSimilarContext(String queryText, UUID companyId, int limit) {
        log.debug("Finding similar context for query (length: {}), company: {}, limit: {}",
                queryText.length(), companyId, limit);

        try {
            // 1. Query-Text in Embedding umwandeln
            float[] queryEmbedding = generateEmbedding(queryText);

            // 2. Vektor-Suche durchführen
            return findSimilarContextByEmbedding(queryEmbedding, companyId, limit);

        } catch (Exception e) {
            log.error("Error during vector search for company: {}", companyId, e);
            return List.of();
        }
    }

    @Override
    public List<Knowledge> findSimilarContextByEmbedding(float[] embedding, UUID companyId, int limit) {
        log.debug("Executing vector search with embedding (dim: {}), company: {}, limit: {}",
                embedding.length, companyId, limit);

        try {
            // PGvector-Objekt erstellen
            PGvector pgVector = new PGvector(embedding);

            // JDBC-Query mit pgvector operator
            List<Knowledge> results = jdbcTemplate.query(
                    VECTOR_SEARCH_SQL,
                    ps -> {
                        ps.setObject(1, pgVector); // Query embedding für Similarity-Berechnung
                        ps.setObject(2, companyId);
                        ps.setObject(3, pgVector); // Query embedding für ORDER BY
                        ps.setInt(4, limit);
                    },
                    this::mapRowToKnowledge
            );

            log.info("Found {} similar knowledge entries for company: {}", results.size(), companyId);
            return results;

        } catch (Exception e) {
            log.error("Error during vector search by embedding for company: {}", companyId, e);
            return List.of();
        }
    }

    @Override
    public List<ScoredKnowledge> findSimilarContextWithScore(
            String queryText,
            UUID companyId,
            int limit,
            double minSimilarity
    ) {
        log.debug("Finding similar context with score (minSimilarity: {}), company: {}, limit: {}",
                minSimilarity, companyId, limit);

        try {
            // 1. Query-Text in Embedding umwandeln
            float[] queryEmbedding = generateEmbedding(queryText);

            // 2. PGvector-Objekt erstellen
            PGvector pgVector = new PGvector(queryEmbedding);

            // 3. JDBC-Query mit Similarity-Threshold
            List<ScoredKnowledge> results = jdbcTemplate.query(
                    VECTOR_SEARCH_WITH_SCORE_SQL,
                    ps -> {
                        ps.setObject(1, pgVector); // Query embedding für Similarity-Berechnung
                        ps.setObject(2, companyId);
                        ps.setObject(3, pgVector); // Query embedding für WHERE-Clause
                        ps.setDouble(4, minSimilarity);
                        ps.setObject(5, pgVector); // Query embedding für ORDER BY
                        ps.setInt(6, limit);
                    },
                    this::mapRowToScoredKnowledge
            );

            log.info("Found {} scored knowledge entries (minSimilarity: {}) for company: {}",
                    results.size(), minSimilarity, companyId);
            return results;

        } catch (Exception e) {
            log.error("Error during scored vector search for company: {}", companyId, e);
            return List.of();
        }
    }

    @Override
    public Knowledge saveWithEmbedding(Knowledge knowledge) {
        log.info("Saving knowledge with embedding for company: {}", knowledge.getCompanyId());

        try {
            // 1. Embedding generieren, falls nicht vorhanden
            if (!knowledge.hasEmbedding()) {
                log.debug("Generating embedding for content (length: {})", knowledge.getContent().length());
                float[] embedding = generateEmbedding(knowledge.getContent());
                knowledge.setEmbedding(embedding);
            }

            // 2. Domain-Modell zu JPA-Entity konvertieren
            KnowledgeJpaEntity entity = mapToJpaEntity(knowledge);

            // 3. Speichern via JPA
            KnowledgeJpaEntity savedEntity = knowledgeRepository.save(entity);

            // 4. Zurück zu Domain-Modell konvertieren
            Knowledge savedKnowledge = mapToDomain(savedEntity);

            log.info("Knowledge saved with id: {}", savedKnowledge.getId());
            return savedKnowledge;

        } catch (Exception e) {
            log.error("Error saving knowledge with embedding for company: {}", knowledge.getCompanyId(), e);
            throw new RuntimeException("Failed to save knowledge with embedding", e);
        }
    }

    /**
     * Generiert ein Embedding für einen Text via OpenAI API.
     *
     * @param text Der zu embeddierende Text
     * @return Das Embedding als float-Array (1536 Dimensionen)
     */
    private float[] generateEmbedding(String text) {
        log.debug("Generating embedding for text (length: {})", text.length());

        try {
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("OPENAI_API_KEY not configured");
            }

            RestClient restClient = restClientBuilder
                    .baseUrl(OPENAI_EMBEDDING_URL)
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "model", OPENAI_EMBEDDING_MODEL,
                    "input", text
            );

            Map<String, Object> response = restClient.post()
                    .uri("")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("data")) {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");

            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }

            log.debug("Embedding generated successfully (dimensions: {})", embedding.length);
            return embedding;

        } catch (Exception e) {
            log.error("Error generating embedding", e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    /**
     * Mappt ResultSet-Row zu Knowledge Domain-Modell.
     */
    private Knowledge mapRowToKnowledge(ResultSet rs, int rowNum) throws SQLException {
        Knowledge knowledge = new Knowledge();
        knowledge.setId((UUID) rs.getObject("id"));
        knowledge.setCompanyId((UUID) rs.getObject("company_id"));
        knowledge.setContent(rs.getString("content"));
        knowledge.setCategory(rs.getString("category"));
        knowledge.setSource(rs.getString("source"));

        // Embedding aus PGvector extrahieren
        PGvector pgVector = (PGvector) rs.getObject("embedding");
        if (pgVector != null) {
            knowledge.setEmbedding(pgVector.toArray());
        }

        return knowledge;
    }

    /**
     * Mappt ResultSet-Row zu ScoredKnowledge.
     */
    private ScoredKnowledge mapRowToScoredKnowledge(ResultSet rs, int rowNum) throws SQLException {
        Knowledge knowledge = mapRowToKnowledge(rs, rowNum);
        double similarity = rs.getDouble("similarity");
        return new ScoredKnowledge(knowledge, similarity);
    }

    /**
     * Mappt Knowledge Domain-Modell zu JPA-Entity.
     */
    private KnowledgeJpaEntity mapToJpaEntity(Knowledge knowledge) {
        KnowledgeJpaEntity entity = KnowledgeJpaEntity.builder()
                .id(knowledge.getId())
                .companyId(knowledge.getCompanyId())
                .content(knowledge.getContent())
                .category(knowledge.getCategory())
                .source(knowledge.getSource())
                .build();

        if (knowledge.hasEmbedding()) {
            entity.setEmbedding(new PGvector(knowledge.getEmbedding()));
        }

        return entity;
    }

    /**
     * Mappt JPA-Entity zu Knowledge Domain-Modell.
     */
    private Knowledge mapToDomain(KnowledgeJpaEntity entity) {
        Knowledge knowledge = new Knowledge();
        knowledge.setId(entity.getId());
        knowledge.setCompanyId(entity.getCompanyId());
        knowledge.setContent(entity.getContent());
        knowledge.setCategory(entity.getCategory());
        knowledge.setSource(entity.getSource());

        if (entity.getEmbedding() != null) {
            knowledge.setEmbedding(entity.getEmbedding().toArray());
        }

        return knowledge;
    }
}
