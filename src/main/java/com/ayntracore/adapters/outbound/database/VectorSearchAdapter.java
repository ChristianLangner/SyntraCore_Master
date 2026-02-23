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
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class VectorSearchAdapter implements VectorSearchPort {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingPort embeddingPort;

    private final RowMapper<KnowledgeEntry> knowledgeRowMapper = (rs, rowNum) -> {
        PGvector embedding = null;
        Object embeddingObject = rs.getObject("embedding");
        if (embeddingObject instanceof PGobject) {
            try {
                embedding = new PGvector(((PGobject) embeddingObject).getValue());
            } catch (SQLException e) {
                log.error("Failed to parse PGvector from PGobject", e);
            }
        } else if (embeddingObject instanceof PGvector) { // Fallback for direct mapping
            embedding = (PGvector) embeddingObject;
        }

        return KnowledgeEntry.builder()
                .id(rs.getObject("id", UUID.class))
                .companyId(rs.getObject("company_id", UUID.class))
                .content(rs.getString("content"))
                .source(rs.getString("source_id"))
                .embedding(embedding != null ? embedding.toArray() : null)
                .build();
    };

    private final RowMapper<ScoredKnowledge> scoredKnowledgeRowMapper = (rs, rowNum) -> {
        try {
            return new ScoredKnowledge(
                knowledgeRowMapper.mapRow(rs, rowNum),
                rs.getDouble("similarity")
            );
        } catch (SQLException e) {
            log.error("Failed to map row to ScoredKnowledge", e);
            return null; 
        }
    };

    @Override
    public List<KnowledgeEntry> findSimilarContext(String queryText, UUID companyId, int limit) {
        long startEmbedding = System.currentTimeMillis();
        PGvector embedding = embeddingPort.createEmbedding(queryText);
        long endEmbedding = System.currentTimeMillis();

        long startDb = System.currentTimeMillis();
        List<KnowledgeEntry> results = findSimilarContextByEmbedding(embedding.toArray(), companyId, limit);
        long endDb = System.currentTimeMillis();

        log.info("[LATENCY] embedding_ms:{} db_query_ms:{}", (endEmbedding - startEmbedding), (endDb - startDb));
        return results;
    }

    @Override
    public List<KnowledgeEntry> findSimilarContextByEmbedding(float[] embedding, UUID companyId, int limit) {
        final String sql = """
            SELECT id, company_id, content, source_id, embedding
            FROM knowledge_entry
            WHERE company_id = ?
            ORDER BY embedding <=> ?
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, knowledgeRowMapper, companyId, new PGvector(embedding), limit);
    }

    @Override
    public List<ScoredKnowledge> findSimilarContextWithScore(String queryText, UUID companyId, int limit, double minSimilarity) {
        long startEmbedding = System.currentTimeMillis();
        PGvector embedding = embeddingPort.createEmbedding(queryText);
        long endEmbedding = System.currentTimeMillis();

        if (embedding == null || embedding.toArray().length == 0) {
            log.warn("Embedding generation for query \"{}\" failed or returned empty. Skipping vector search.", queryText);
            return Collections.emptyList();
        }

        final String sql = """
            SELECT id, company_id, content, source_id, embedding, 1 - (embedding <=> ?) AS similarity
            FROM knowledge_entry
            WHERE company_id = ? AND 1 - (embedding <=> ?) > ?
            ORDER BY similarity DESC
            LIMIT ?
            """;
        Object[] args = {new PGvector(embedding.toArray()), companyId, new PGvector(embedding.toArray()), minSimilarity, limit};
        
        long startDb = System.currentTimeMillis();
        List<ScoredKnowledge> results = jdbcTemplate.query(sql, scoredKnowledgeRowMapper, args);
        long endDb = System.currentTimeMillis();
        
        log.info("[LATENCY] embedding_ms:{} db_query_ms:{}", (endEmbedding - startEmbedding), (endDb - startDb));
        return results;
    }

    @Override
    public KnowledgeEntry saveWithEmbedding(KnowledgeEntry knowledge) {
        long embeddingLatency = 0;
        if (knowledge.getEmbedding() == null) {
            log.debug("Embedding is missing for knowledge entry with id: {}. A new embedding will be generated.", knowledge.getId());
            long startEmbedding = System.currentTimeMillis();
            PGvector embeddingVector = embeddingPort.createEmbedding(knowledge.getContent());
            knowledge.setEmbedding(embeddingVector.toArray());
            embeddingLatency = System.currentTimeMillis() - startEmbedding;
        }

        final String sql = "INSERT INTO knowledge_entry (id, company_id, content, source_id, embedding, created_at) VALUES (?, ?, ?, ?, ?, NOW()) ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content, embedding = EXCLUDED.embedding, source_id = EXCLUDED.source_id, created_at = COALESCE(knowledge_entry.created_at, NOW())";
        
        long startDb = System.currentTimeMillis();
        jdbcTemplate.update(sql,
                knowledge.getId(),
                knowledge.getCompanyId(),
                knowledge.getContent(),
                knowledge.getSource(),
                new PGvector(knowledge.getEmbedding())
        );
        long endDb = System.currentTimeMillis();

        log.info("[LATENCY] embedding_ms:{} db_query_ms:{}", embeddingLatency, (endDb - startDb));
        log.info("Successfully saved/updated knowledge entry {} for company {}.", knowledge.getId(), knowledge.getCompanyId());
        return knowledge;
    }
}
