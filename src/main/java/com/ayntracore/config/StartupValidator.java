package com.ayntracore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Slf4j
@RequiredArgsConstructor
public class StartupValidator implements CommandLineRunner {

    private final DataSource dataSource;

    @Value("${ai.openrouter.key}")
    private String openAiApiKey;

    @Value("${ayntra.persona.company-id}")
    private String companyId;

    @Override
    public void run(String... args) throws Exception {
        log.info("--- STARTUP VALIDATION ---");

        // 1. Neon DB Check
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                log.info("[NEON-CONNECT] SUCCESS");
            } else {
                log.error("[NEON-CONNECT] FAILED - Connection is not valid.");
            }
        } catch (Exception e) {
            log.error("[NEON-CONNECT] FAILED - Could not connect to Neon: {}", e.getMessage());
        }

        // 2. AI Provider Check
        if (openAiApiKey != null && !openAiApiKey.isBlank()) {
            log.info("[AI-CHECK] OK");
        } else {
            log.error("[AI-CHECK] FAILED - 'ai.openrouter.key' is not loaded.");
        }

        // 3. Config Check
        if (companyId != null && !companyId.isBlank()) {
            log.info("[CONFIG-CHECK] OK");
        } else {
            log.error("[CONFIG-CHECK] FAILED - 'ayntra.persona.company-id' is not set.");
        }
        log.info("--- VALIDATION COMPLETE ---");
    }
}
