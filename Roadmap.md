# 🗺️ AyntraCore Project Roadmap

This document tracks the major development milestones of the AyntraCore project.

---

## ✅ Completed Milestones

### Phase 1: Core Architecture & Foundation (COMPLETED)
- **[COMPLETED]** Initial project setup with Spring Boot and Maven.
- **[COMPLETED]** Implementation of the Hexagonal Architecture (Ports & Adapters).
- **[COMPLETED]** Basic domain models for `Persona`, `SupportTicket`.

### Phase 2: AI & RAG Integration (COMPLETED)
- **[COMPLETED]** Integration of the OpenAI API via an outbound adapter.
- **[COMPLETED]** Initial implementation of the RAG (Retrieval-Augmented Generation) workflow.

### Phase 3: Database & Multi-Tenancy (COMPLETED)
- **[COMPLETED]** PostgreSQL integration with `pgvector` for vector similarity search.
- **[COMPLETED]** Full multi-tenancy support using a `companyId` in all relevant domain models and database queries.
- **[COMPLETED]** `Domain Harmonization`: `Knowledge` object was replaced by `KnowledgeEntry` to create a single source of truth.
- **[COMPLETED]** `Vector Search Integration`: The `VectorSearchAdapter` was fully integrated to use `pgvector` and the `EmbeddingPort`.

---

## 🚀 Future Milestones

### Phase 4: Advanced Features & Production Readiness
- **[PENDING]** WebSocket implementation for real-time communication.
- **[PENDING]** Enhanced content safety and moderation features.
- **[PENDING]** Comprehensive end-to-end testing suite.
