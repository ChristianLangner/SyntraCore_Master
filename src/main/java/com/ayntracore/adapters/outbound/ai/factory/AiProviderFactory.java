// Autor: Christian Langner
package com.ayntracore.adapters.outbound.ai.factory;

import com.ayntracore.core.domain.AiProvider;
import com.ayntracore.core.ports.UniversalAiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory für die Auswahl des richtigen AI-Providers basierend auf der Konfiguration.
 */
@Configuration
@RequiredArgsConstructor
public class AiProviderFactory {

    private final List<UniversalAiPort> providers;
    @Value("${ayntracore.ai.provider:OPENAI}")
    private String defaultProvider;

    @Bean
    @Primary
    public UniversalAiPort universalAiPort() {
        AiProvider providerEnum = AiProvider.valueOf(defaultProvider.toUpperCase());

        return providers.stream()
                .filter(p -> {
                    if (providerEnum == AiProvider.OPENAI) return p.getClass().getSimpleName().contains("OpenAi");
                    if (providerEnum == AiProvider.DEEPSEEK) return p.getClass().getSimpleName().contains("DeepSeek");
                    return false;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No AI provider found for: " + defaultProvider));
    }

    @Bean
    public Map<AiProvider, UniversalAiPort> providerRegistry() {
        return providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getClass().getSimpleName().contains("OpenAi") ? AiProvider.OPENAI : AiProvider.DEEPSEEK,
                        p -> p
                ));
    }
}
