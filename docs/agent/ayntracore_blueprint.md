
# AyntraCore Technical Blueprint & Coding Standards

## 1. Architektur-Zielsatz (Hexagonal Design)
Das System folgt strikt dem **Ports & Adapters**-Muster.

### Core Domain
- Enthält reine Geschäftslogik ohne Framework-Abhängigkeiten
- **Kein JPA, kein Spring** im Core

### Application Layer
- Orchestriert Use-Cases
- Implementiert Retrieval-Augmented Generation (RAG)

### Infrastructure Layer
- Adapter für:
  - Datenbanken
  - AI / LLM Provider
  - Web-APIs

---

## 2. Coding Style & Dokumentations-Vorgaben

### Sprache
- **Java 21**, Nutzung von `record` für DTOs & Domain-Modelle

### Boilerplate-Reduktion
- **Lombok**: `@Data`, `@Builder`, `@RequiredArgsConstructor`

### Logging
- **Strukturiertes SLF4J-Logging**
- VectorSearchAdapter loggt:
  - Embedding-Length
  - Cosine Similarity Scores
  - Query-Latenzen

---

## 3. Datenbank- & Sicherheits-Design (Multi-Tenancy)

### Multi-Tenant Aufbau
- Jede Entität besitzt eine **company_id (UUID)**.

### Sicherheitsregel
- **Abfragen ohne company_id-Filter = @Deprecated**
- Dürfen NICHT verwendet werden.

### Vektoren
- Nutzung der **pgvector**-Extension (z. B. Neon PostgreSQL)
- Similarity-Suche: Cosine Similarity

---

## 4. RAG & KI-Workflow-Struktur

### Retrieval
- Relevanteste Wissenseinträge via Embedding-Suche

### Augmentation
- Kontext wird automatisch in den Prompt eingebettet

### Generation
- Aufruf der Modelle (DeepSeek, OpenAI etc.) über:
  - **UniversalAiPort-Interface**

---

## 5. Frontend-Vorgaben

### Theming
- Dynamische CSS-Variablen basierend auf Persona-`traits` (JSONB)

### Feedback
- Typing-Indikatoren
- Anzeige der verwendeten RAG-Quellen

---

## Empfohlene Formate
- **Markdown (.md)** → Beste Wahl für Agenten & Entwickler
- **Word (.docx)** → Enterprise-Dokumente
- **PDF** → Finaler, unveränderlicher Source of Truth

