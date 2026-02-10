package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.Knowledge;
import com.ayntracore.core.ports.VectorSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Outbound Adapter for vector-based similarity search using pgvector.
 *
 * <p><strong>Architecture Layer:</strong> Infrastructure Layer (Outbound Adapter)</p>
 * <p><strong>Hexagonal Architecture:</strong> Implements VectorSearchPort</p>
 */
@Component
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class VectorSearchAdapter implements VectorSearchPort {

    private final JdbcTemplate jdbcTemplate;

    // Maps a database row to a Knowledge domain object.
    private final RowMapper<Knowledge> rowMapper = (rs, rowNum) -> Knowledge.builder()
            .id(rs.getLong("id"))
            .companyId(rs.getObject("company_id", UUID.class))
            .category(rs.getString("category"))
            .content(rs.getString("content"))
            .similarity(1 - rs.getFloat("distance")) // Calculate similarity from cosine distance
            .build();

    @Override
    public List<Knowledge> search(UUID companyId, float[] embedding, int topK, double minSimilarity) {
        final String sql = """
            SELECT
                id,
                company_id,
                category,
                content,
                embedding <=> ? AS distance
            FROM
                knowledge
            WHERE
                company_id = ? AND 1 - (embedding <=> ?) > ?
            ORDER BY
                distance ASC
            LIMIT ?
            """;

        // The embedding needs to be passed in pgvector format.
        String pgvectorEmbedding = toPgVectorString(embedding);

        long startTime = System.nanoTime();
        List<Knowledge> matches = jdbcTemplate.query(
                sql,
                rowMapper,
                pgvectorEmbedding,
                companyId,
                pgvectorEmbedding,
                minSimilarity,
                topK
        );
        long dbQueryMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        log.info("[LATENCY] db_query_ms:{}", dbQueryMs);

        // Structured logging of search results as per specifications.
        logStructuredMatches(matches);

        return matches;
    }

    /**
     * Logs each vector search match according to the defined structured logging format.
     * Differentiates between relevant matches and low-relevance warnings.
     *
     * @param matches The list of Knowledge objects returned from the vector search.
     */
    private void logStructuredMatches(List<Knowledge> matches) {
        log.debug("Logging {} vector search matches for quality assurance.", matches.size());
        for (Knowledge match : matches) {
            double similarity = match.getSimilarity();
            // Log every match with INFO level.
            log.info("[VECTOR_MATCH] ID:{} Similarity:{} Category:{}",
                    match.getId(),
                    String.format("%.4f", similarity),
                    match.getCategory()
            );

            // Add a specific WARNING for low-relevance matches.
            if (similarity < 0.70) {
                log.warn("[LOW_RELEVANCE_WARNING] ID:{} Similarity:{}",
                        match.getId(),
                        String.format("%.4f", similarity)
                );
            }
        }
    }


    /**
     * Converts a float array into a string representation compatible with pgvector.
     * Example: [0.1, 0.2, 0.3] -> "[0.1,0.2,0.3]"
     *
     * @param embedding The embedding vector.
     * @return A string representation for use in SQL queries.
     */
    private String toPgVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
