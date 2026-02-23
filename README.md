# 🚀 AyntraCore – Enterprise Multi-Tenant Persona & RAG Engine

**Architektur-Level:** Hexagonal (Ports & Adapters) | **Phase:** 4.2 (Multi-Persona UI & Hardening)  
**Stack:** Java 21 | Spring Boot 3.2.3 | PostgreSQL + pgvector for RAG | Jakarta EE | jQuery
**Zielgruppe:** IHK-Prüfungskandidaten, Enterprise Architekten, AI-Engineer

---

## 📋 Inhaltsverzeichnis

1. [Architektur-Status](#architektur-status)
2. [Architektur-Philosophie](#architektur-philosophie)
3. [Schichtenmodell Deep Dive](#schichtenmodell-deep-dive)
4. [Kernkomponenten & Business Logic](#kernkomponenten-business-logic)
5. [Hexagonale Architektur in der Praxis](#hexagonale-architektur-in-der-praxis)
6. [Mandantenfähigkeit (Multi-Tenancy)](#mandantenfähigkeit-multi-tenancy)
7. [RAG-Workflow Technologie](#rag-workflow-technologie)
8. [VS Code Setup (IntelliJ-Experience)](#vs-code-setup-intellij-experience)
9. [Einrichtung & Quick Start](#einrichtung-quick-start)
10. [Deployment & Profile](#deployment-profile)

---

## 🏛️ Architektur-Status

### **Meilenstein (v4.2): Multi-Persona-UI & Fehlerbehebung**

- **Frontend:** Eine neue, dynamische Benutzeroberfläche (`index.html` + `app.js`) wurde implementiert. Benutzer können nun live zwischen verschiedenen KI-Personas (z.B. "Astra" für Text, "Seraphina" für Bilder) wechseln.
- **Backend-Härtung:** Der `ImageGenerationService` wurde mit einem Guard-Clause versehen, um fehlerhafte Anfragen von nicht-visuellen Personas proaktiv abzufangen. Dies erhöht die Stabilität und verhindert unnötige API-Aufrufe.
- **Bugfix:** Ein kritischer Fehler im Frontend, der fälschlicherweise Bildgenerierungs-Anfragen für reine Text-Personas auslöste, wurde behoben. Die `mode`-Einstellung (`text` vs. `image`) wird nun zuverlässig basierend auf den Fähigkeiten der aktiven Persona gesetzt.
- **KI-Agent-Integration:** Das Projekt ist nun für die Verwendung mit externen Debugging- und Code-Analyse-Tools wie dem "Kilo-Agenten" vorkonfiguriert (`.kilocode.json`).

### **Meilenstein (v4.0): Stabile RAG-Pipeline & Datenbank-Integrität**

- **Datenbank:** Wir nutzen **Neon Postgres (Serverless)** in Kombination mit der `pgvector`-Erweiterung für die Speicherung und Abfrage von Vektor-Embeddings.
- **Resilienz:** Ein Feature sorgt für Ausfallsicherheit. Sollte eine primäre KI-API nicht erreichbar sein, kann das System auf ein Fallback-Modell umschalten.

---

## 🎯 Architektur-Philosophie

AyntraCore folgt der **Hexagonalen Architektur** (auch "Ports & Adapters" genannt). Dies ist das _de facto_-Standard-Muster für moderne Enterprise-Anwendungen, die Framework-Abhängigkeiten vermeiden wollen.

- **Framework-Unabhängigkeit:** Der Core (Domain + Application) ist frei von Spring-, JPA- oder anderen Framework-Abhängigkeiten.
- **Testbarkeit:** Jede Schicht kann isoliert mit Mock-Adaptern getestet werden.
- **Business-Fokus:** Der Core beschreibt _was_ das System tut, nicht _wie_.

---

## 🏗️ Schichtenmodell Deep Dive

### **Schicht 1: Domain Layer (Geschäftslogik)**
_Pfad:_ `com.ayntracore.core.domain`

- **`Persona`**: Multi-Tenant-Identität eines Chatbots (`companyId`, `systemPrompt`, `traits`).
- **`KnowledgeEntry`**: Wissensbasis-Eintrag mit Vektor-Embedding (`companyId`, `content`, `embedding`).
- **`SupportTicket`**: Customer-Support-Anfrage.

Diese Klassen sind **Framework-unabhängig**.

### **Schicht 2: Application Layer (Geschäftsfälle)**
_Pfad:_ `com.ayntracore.core.application`

- **`RAGCoordinationService`**: Orchestriert den RAG-Workflow.
- **`ImageGenerationService`**: Generiert Bilder basierend auf Persona-Fähigkeiten.
- **`ContentSafetyService`**: Filtert schädliche Inhalte.
- **`PersonaService`**: Verwaltet die Personas.

### **Schicht 3: Ports (Schnittstellen-Verträge)**
_Pfad:_ `com.ayntracore.core.ports`

Definiert die Schnittstellen für Outbound-Kommunikation (z.B. `KnowledgeBasePort`, `PersonaRepositoryPort`).

### **Schicht 4: Adapters (Konkrete Implementierungen)**
_Pfad:_ `com.ayntracore.adapters`

- **Inbound:** `AgentController` (REST), `ChatController` (WebSocket).
- **Outbound:** `VectorSearchAdapter` (pgvector), `OpenAiAdapter`/`DeepSeekAdapter` (LLMs), `PersonaPersistenceAdapter` (DB).

---

## 👥 Mandantenfähigkeit (Multi-Tenancy)

Die Anwendung ist vollständig mandantenfähig. Jede Entität ist über eine `companyId` isoliert, um Cross-Tenant-Datenzugriff zu verhindern. SQL-Abfragen erzwingen diesen Filter auf Datenbankebene.

---

## 🤖 RAG-Workflow Technologie

Der Prozess folgt dem Standard-RAG-Muster: Query Embedding → Vector Search → Knowledge Retrieval → Context Augmentation → Safety Check → LLM Call.

---

## 💻 VS Code Setup (IntelliJ-Experience)

Konfigurationsdateien für `settings.json` und `launch.json` sind im Projekt enthalten, um ein nahtloses Entwicklungs- und Debugging-Erlebnis in VS Code zu ermöglichen.

---

## 🚀 Einrichtung & Quick Start

1.  **Setup ausführen:** `./src/setup.ps1` (Windows/WSL) oder manuell `chmod +x mvnw`.
2.  **Environment-Variablen setzen:** Erstellen Sie eine `.env`-Datei mit `OPENAI_API_KEY` und `DATABASE_URL`.
3.  **Build:** `./mvnw clean compile -DskipTests`
4.  **Anwendung starten:**
    -   `./mvnw spring-boot:run --spring.profiles.active=school` (für lokale H2-Datenbank)
    -   `./mvnw spring-boot:run --spring.profiles.active=home` (für Neon PostgreSQL)

---

## 📊 Deployment & Profile

| Profile | Datenbank | Vector-Search | Einsatzgebiet |
| --- | --- | --- | --- |
| `school` | H2 In-Memory | Mock/Disabled | Entwicklung, Tests |
| `home` | Neon PostgreSQL | pgvector | Production |

---

**Entwickelt von:** Christian Langner  
**Version:** 4.2 | Multi-Persona UI & Hardening 
**Letzte Aktualisierung:** März 2026
