package com.ayntracore.adapters.outbound.database;

import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.ports.EmbeddingPort;
import com.ayntracore.core.ports.VectorSearchPort;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;


@Component
@Profile("home") 
@RequiredArgsConstructor
@Slf4j
public class VectorSearchAdapter implements VectorSearchPort {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingPort embeddingPort;

    // Use the builder from Lombok to map the row to the object.
    private final RowMapper<KnowledgeEntry> knowledgeRowMapper = (rs, rowNum) -> KnowledgeEntry.builder()
            .id(rs.getObject("id", UUID.class))
            .companyId(rs.getObject("company_id", UUID.class))
            .category(rs.getString("category"))
            .content(rs.getString("content"))
            .source(rs.getString("source"))
            .embedding(rs.getObject("embedding", PGvector.class) != null ? rs.getObject("embedding", PGvector.class).toArray() : null)
            .build();

    // The ScoredKnowledge record uses the above RowMapper.
    private final RowMapper<ScoredKnowledge> scoredKnowledgeRowMapper = (rs, rowNum) -> {
        try {
            return new ScoredKnowledge(
                knowledgeRowMapper.mapRow(rs, rowNum),
                rs.getDouble("similarity")
            );
        } catch (SQLException e) {
            log.error("Failed to map row to ScoredKnowledge", e);
            return null; // Or throw a custom exception
        }
    };

    @Override
    public List<KnowledgeEntry> findSimilarContext(String queryText, UUID companyId, int limit) {
        PGvector embedding = embeddingPort.createEmbedding(queryText);
        return findSimilarContextByEmbedding(embedding.toArray(), companyId, limit);
    }

    @Override
    public List<KnowledgeEntry> findSimilarContextByEmbedding(float[] embedding, UUID companyId, int limit) {
        final String sql = """
            SELECT id, company_id, category, content, source, embedding
            FROM knowledge_entry
            WHERE company_id = ?
            ORDER BY embedding <=> ?
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, knowledgeRowMapper, companyId, new PGvector(embedding), limit);
    }

    @Override
    public List<ScoredKnowledge> findSimilarContextWithScore(String queryText, UUID companyId, int limit, double minSimilarity) {
        PGvector embedding = embeddingPort.createEmbedding(queryText);
        final String sql = """
            SELECT id, company_id, category, content, source, embedding, 1 - (embedding <=> ?) AS similarity
            FROM knowledge_entry
            WHERE company_id = ? AND 1 - (embedding <=> ?) > ?
            ORDER BY similarity DESC
            LIMIT ?
            """;
        Object[] args = {new PGvector(embedding.toArray()), companyId, new PGvector(embedding.toArray()), minSimilarity, limit};
        return jdbcTemplate.query(sql, scoredKnowledgeRowMapper, args);
    }

    @Override
    public KnowledgeEntry saveWithEmbedding(KnowledgeEntry knowledge) {
        // Use GETTER to check if embedding exists.
        if (knowledge.getEmbedding() == null) {
            log.debug("Embedding is missing for knowledge entry with id: {}. A new embedding will be generated.", knowledge.getId());
            PGvector embeddingVector = embeddingPort.createEmbedding(knowledge.getContent());
            // Use SETTER to update the embedding.
            knowledge.setEmbedding(embeddingVector.toArray());
        }

        final String sql = "INSERT INTO knowledge_entry (id, company_id, category, content, source, embedding) VALUES (?, ?, ?, ?, ?, ?)";
        
        // Use GETTERS for the update.
        jdbcTemplate.update(sql,
                knowledge.getId(),
                knowledge.getCompanyId(),
                knowledge.getCategory(),
                knowledge.getContent(),
                knowledge.getSource(),
                new PGvector(knowledge.getEmbedding())
        );
        
        log.info("Successfully saved knowledge entry {} for company {}.", knowledge.getId(), knowledge.getCompanyId());
        return knowledge;
    }
}
