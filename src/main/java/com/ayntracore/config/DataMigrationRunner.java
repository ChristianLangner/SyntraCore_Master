package com.ayntracore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("home")
@RequiredArgsConstructor
@Slf4j
public class DataMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data migration for knowledge_entry.created_at...");

        String sql = "UPDATE knowledge_entry SET created_at = NOW() WHERE created_at IS NULL";
        int updatedRows = jdbcTemplate.update(sql);

        if (updatedRows > 0) {
            log.info("Successfully updated {} rows in knowledge_entry with a created_at timestamp.", updatedRows);
        } else {
            log.info("No rows in knowledge_entry needed a created_at timestamp update.");
        }

        log.info("Data migration for knowledge_entry.created_at completed.");
    }
}
