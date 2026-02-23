package com.ayntracore.config;

import com.ayntracore.core.application.ImageGenerationService;
import com.ayntracore.core.application.RAGCoordinationService;
import com.ayntracore.core.domain.KnowledgeEntry;
import com.ayntracore.core.ports.KnowledgeBasePort;
import com.ayntracore.core.ports.PersonaOutputPort;
import com.ayntracore.core.ports.VectorSearchPort;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public VectorSearchPort vectorSearchPort() {
        return new VectorSearchPort() {
            @Override
            public List<KnowledgeEntry> findSimilarContext(String queryText, UUID companyId, int limit) {
                return Collections.emptyList();
            }

            @Override
            public List<KnowledgeEntry> findSimilarContextByEmbedding(float[] embedding, UUID companyId, int limit) {
                return Collections.emptyList();
            }

            @Override
            public List<ScoredKnowledge> findSimilarContextWithScore(String queryText, UUID companyId, int limit, double minSimilarity) {
                return Collections.emptyList();
            }

            @Override
            public KnowledgeEntry saveWithEmbedding(KnowledgeEntry knowledge) {
                return knowledge;
            }
        };
    }

    @Bean
    @Primary
    public ImageGenerationService imageGenerationService() {
        return Mockito.mock(ImageGenerationService.class);
    }

    @Bean
    @Primary
    public RAGCoordinationService ragCoordinationService() {
        return Mockito.mock(RAGCoordinationService.class);
    }

    @Bean
    @Primary
    public PersonaOutputPort personaOutputPort() {
        return Mockito.mock(PersonaOutputPort.class);
    }

    @Bean
    @Primary
    public KnowledgeBasePort knowledgeBasePort() {
        return Mockito.mock(KnowledgeBasePort.class);
    }
}
