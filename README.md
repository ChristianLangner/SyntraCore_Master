# 🚀 AyntraCore – Enterprise Multi-Tenant Persona & RAG Engine

**Architektur-Level:** Hexagonal (Ports & Adapters) | **Phase:** 4.0 (Finalized, Documented)  
**Stack:** Java 21 | Spring Boot 3.2.3 | PostgreSQL + pgvector for RAG | Jakarta EE  
**Zielgruppe:** IHK-Prüfungskandidaten, Enterprise Architekten, AI-Engineer

---

## 📋 Inhaltsverzeichnis
1. [Architektur-Philosophie](#architektur-philosophie)
2. [Schichtenmodell Deep Dive](#schichtenmodell-deep-dive)
3. [Kernkomponenten & Business Logic](#kernkomponenten-business-logic)
4. [Hexagonale Architektur in der Praxis](#hexagonale-architektur-in-der-praxis)
5. [Mandantenfähigkeit (Multi-Tenancy)](#mandantenfähigkeit-multi-tenancy)
6. [RAG-Workflow Technologie](#rag-workflow-technologie)
7. [VS Code Setup (IntelliJ-Experience)](#vs-code-setup-intellij-experience)
8. [Einrichtung & Quick Start](#einrichtung-quick-start)
9. [Deployment & Profile](#deployment-profile)

---

## 🎯 Architektur-Philosophie

AyntraCore folgt der **Hexagonalen Architektur** (auch "Ports & Adapters" genannt), entwickelt von Alistair Cockburn. Dies ist das _de facto_-Standard-Muster für moderne Enterprise-Anwendungen, die Framework-abhängig werdenden Altlasten vermeiden wollen.

### Warum Hexagonal?
- **Framework-Unabhängigkeit:** Der Core (Domain + Application) enthält _keine_ Abhängigkeiten zu Spring, JPA oder anderen Frameworks.
- **Testbarkeit:** Jede Schicht kann isoliert mit Mock-Adaptern getestet werden.
- **Skalierbarkeit:** Neue Adapter (z.B. REST, GraphQL, gRPC) können ohne Änderung des Cores hinzugefügt werden.
- **Business-Fokus:** Der Core beschreibt _was_ das System tut, nicht _wie_.

---

## 🏗️ Schichtenmodell Deep Dive

### **Schicht 1: Domain Layer (Geschäftslogik)**
_Pfad:_ `com.ayntracore.core.domain`

**Zweck:** Repräsentiert die reinen Geschäftsregeln ohne technische Abhängigkeiten.

**Kernentitäten:**
- **`Persona`**: Multi-Tenant-Identität eines Chatbots
  - `companyId`: Zugehörigkeit zum Mandanten (Tenant)
  - `systemPrompt`: Basis-Charakterisierung (z.B. "Du bist ein Support-Bot")
  - `traits`: JSON-Map für flexible Eigenschaften
  - `promptTemplate`: Dynamischer Prompt mit Platzhaltern
  
- **`KnowledgeEntry`**: Wissensbasis-Eintrag mit Vektor-Embedding
  - `companyId`: Tenant-Bezug
  - `content`: Der eigentliche Inhaltstext
  - `embedding`: float[] (1536-dim für OpenAI text-embedding-3-small)
  
- **`SupportTicket`**: Customer-Support-Anfrage
  - `companyId`: Tenant-Bezug (Sicherheit!)
  - `customerName`, `message`: Anfrage-Details
  - `aiAnalysis`: RAG-generierte AI-Antwort

**Wichtig:** Diese Klassen sind **Framework-unabhängig**. Sie importieren _kein_ `jakarta.persistence` oder `org.springframework`.

---

### **Schicht 2: Application Layer (Geschäftsfälle)**
_Pfad:_ `com.ayntracore.core.application`

**Kernservices:**

- **`RAGCoordinationService`** – Der Gehirn-Orchestrator
  - Koordiniert Retrieval → Augmentation → Generation
  - Input: `AiChatRequest` (customerMessage, companyId)
  - Output: `AiResponse` (generatedText, confidence, tokenUsage)

- **`ContentSafetyService`** – Security Layer
  - Prüft auf Prompt-Injection-Attacks
  - Filtert NSFW-Inhalte basierend auf Persona-Policy

- **`PersonaService`** – Persona-Management
  - Lädt aktive Persona pro Company
  - Generiert dynamische System-Prompts

---

### **Schicht 3: Ports (Schnittstellen-Verträge)**
_Pfad:_ `com.ayntracore.core.ports`

**Outbound Ports** (rufen externe Systeme auf):
- `KnowledgeBasePort`: Wissensdatenbank-Zugriff (mit Vektor-Suche)
- `PersonaRepositoryPort`: Persona-Persistung mit `findActiveByCompanyId()`
- `TicketRepositoryPort`: Ticket-Persistung mit **Mandanten-Filterung** (`findAllByCompanyId()`, `findByIdAndCompanyId()`)

---

### **Schicht 4: Adapters (Konkrete Implementierungen)**
_Pfad:_ `com.ayntracore.adapters`

**Inbound Adapters:**
- `ChatController`: WebSocket Endpoint für Chat-Anfragen
- `AdminController`: Admin-UI Konfiguration

**Outbound Adapters:**
- `VectorSearchAdapter`: PostgreSQL + pgvector Integration
- `OpenAiAdapter` / `DeepSeekAdapter`: LLM-Gateway
- `TicketDatabaseAdapter`: JPA-Persistierung mit Mandanten-Filterung
- `PersonaPersistenceAdapter`: Persona-Persistierung

---

## 👥 Mandantenfähigkeit (Multi-Tenancy)

AyntraCore ist vollständig **Multi-Tenant-ready**. Jede Domain-Entität hat eine `companyId` UUID.

**Sicherheitsmechanismus:**

```java
// ✅ SAFE: Nur Tickets für diese Company
List<SupportTicket> findAllByCompanyId(UUID companyId);

// ✅ SAFE: Verhindert Cross-Tenant-Access
Optional<SupportTicket> findByIdAndCompanyId(UUID id, UUID companyId);

// ❌ DEPRECATED: Unsicher, gibt alle Tickets zurück
@Deprecated
List<SupportTicket> findAll();
```

In SQL wird dies erzwungen:
```sql
-- ✅ Always include company_id in WHERE clause
SELECT * FROM tickets 
WHERE id = $1 AND company_id = $2;
```

---

## 🤖 RAG-Workflow Technologie

**RAG = Retrieval-Augmented Generation**

```
Customer-Input (Text)
    ↓
[1] QUERY EMBEDDING (OpenAI text-embedding-3-small)
    ↓
[2] VECTOR SEARCH (PostgreSQL pgvector, Cosine-Similarity)
    ↓
[3] KNOWLEDGE RETRIEVAL (Top-3 Resultate)
    ↓
[4] CONTEXT AUGMENTATION (Zusammenstellung des Kontexts)
    ↓
[5] CONTENT SAFETY CHECK (Prompt-Injection Filter)
    ↓
[6] LLM CALL (OpenAI/DeepSeek mit System-Prompt von Persona)
    ↓
Generated Response (Text)
```

---

## 💻 VS Code Setup (IntelliJ-Experience)

### **.vscode/settings.json**
```json
{
  "editor.fontFamily": "'JetBrains Mono', 'Fira Code', monospace",
  "editor.fontLigatures": true,
  "editor.fontSize": 13,
  "editor.lineHeight": 1.5,
  "workbench.colorTheme": "Default Dark Modern",
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.import.maven.enabled": true
}
```

### **.vscode/launch.json – Debug Profiles**
```json
{
  "configurations": [
    {
      "name": "AyntraCore: HOME (Neon PostgreSQL)",
      "type": "java",
      "mainClass": "com.ayntracore.AyntraCoreApplication",
      "args": "--spring.profiles.active=home"
    },
    {
      "name": "AyntraCore: SCHOOL (H2 In-Memory)",
      "type": "java",
      "mainClass": "com.ayntracore.AyntraCoreApplication",
      "args": "--spring.profiles.active=school"
    }
  ]
}
```

---

## 🚀 Einrichtung & Quick Start

### **Schritt 1: Setup ausführen**
```bash
cd /workspaces/AyntraCore_Master

# PowerShell (Windows/WSL)
./src/setup.ps1

# Oder manuell (Linux/Mac):
chmod +x mvnw
rm -rf target bin .idea
mkdir -p .vscode
```

### **Schritt 2: Environment-Variablen setzen**
```bash
# .env (wird gitignored)
OPENAI_API_KEY=sk-...
DATABASE_URL=postgresql://...
```

### **Schritt 3: Build durchführen**
```bash
./mvnw clean compile -DskipTests
```

### **Schritt 4: Anwendung starten**
```bash
# Option A: Lokal mit H2
./mvnw spring-boot:run --spring.profiles.active=school

# Option B: Cloud mit PostgreSQL
./mvnw spring-boot:run --spring.profiles.active=home

# Oder in VS Code: F5 (nutze launch.json profile)
```

---

## 📊 Deployment & Profile

| Profile | Datenbank | Vector-Search | Einsatzgebiet |
| --- | --- | --- | --- |
| `school` | H2 In-Memory | Mock/Disabled | Entwicklung, Tests |
| `home` | Neon PostgreSQL | pgvector | Production |

---

**Entwickelt von:** Christian Langner  
**Version:** 4.0 | Multi-Tenancy, RAG, Enterprise-Ready, Finalized & Documented 
**Letzte Aktualisierung:** Februar 2026
