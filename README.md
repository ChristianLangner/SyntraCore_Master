# 🚀 AyntraCore – Enterprise Multi-Tenant Persona & RAG Engine

**Architektur-Level:** Hexagonal (Ports & Adapters)  
**Entwicklungs-Phase:** 3.0 (Resilience, Multi-Tenancy & Vector-Search)  
**Technologie-Stack:** Java 21, Spring Boot 3.2.3, PostgreSQL (pgvector)

---

## 1. Architektur & Design-Philosophie (IHK-Prüfungsniveau)

AyntraCore ist nach dem Prinzip der **Hexagonalen Architektur** entworfen. Dies stellt sicher, dass der fachliche Kern (Core) vollständig isoliert von externen Frameworks, Datenbanken oder KI-Providern bleibt.



### 1.1 Schichtenmodell
* **Domain Layer (Core):** Enthält die reinen Geschäftsregeln und POJOs. Frei von Spring- oder JPA-Annotationen.
* **Application Layer:** Orchestriert Use-Cases wie die RAG-Koordination oder Bildgenerierung.
* **Infrastructure Layer (Adapters):** Implementiert technische Details. Inbound-Adapter (Web/WebSocket) und Outbound-Adapter (Persistence/AI).

---

## 2. Deep Dive: Wichtigste Klassen & Funktionen
*Diese Sektion dient der schnellen Orientierung für KI-Agenten und neue Entwickler.*

### 🧠 Core & Business Logic
| Klasse | Pfad | Funktion & Business Logic |
| :--- | :--- | :--- |
| `Persona` | `core.domain` | **Identitäts-Anker:** Definiert Charakter, Sprachstil und dynamische JSON-Traits. Steuert über `PromptTemplates` die KI-Persona. |
| `Knowledge` | `core.domain` | **Wissens-Modell:** Hält den Content und die 1536-dimensionalen Vektor-Embeddings für die semantische Suche. |
| `RAGCoordinationService` | `core.application` | **Der Gehirn-Orchestrator:** Koordiniert den RAG-Workflow: Retrieval (Vektorsuche) -> Augmentation (Kontext) -> Generation (LLM). |
| `ContentSafetyService` | `core.application` | **Sicherheits-Layer:** Prüft auf Prompt-Injections und filtert explizite Inhalte basierend auf der Persona-Policy. |

### 🔌 Adapters (Infrastruktur)
| Klasse | Pfad | Technische Umsetzung |
| :--- | :--- | :--- |
| `VectorSearchAdapter` | `adapters.outbound` | **Vektor-Brücke:** Implementiert `pgvector` Operationen (Cosine Distance) für die semantische Suche in PostgreSQL. |
| `OpenAiAdapter` | `adapters.outbound` | **LLM-Gateway:** Transformiert Core-Requests in API-Aufrufe für OpenAI/DeepSeek (via OpenRouter). |
| `ChatController` | `adapters.inbound` | **Echtzeit-Port:** Verarbeitet WebSocket-STOMP-Nachrichten für den Live-Chat. |

---

## 3. VS Code "IntelliJ-Experience" Setup

Um in VS Code eine IntelliJ-ähnliche Produktivität und Ästhetik zu erreichen, führt das `setup.ps1` Skript automatisch folgende Schritte aus:

### 3.1 Automatischer Build & Maven
Das Skript konfiguriert den Workspace so, dass Maven-Abhängigkeiten aus der `pom.xml` automatisch erkannt und indiziert werden.
* **Java Language Support:** Aktiviert automatische Build-Konfiguration.
* **Maven Wrapper Fix:** Stellt Berechtigungen für `./mvnw` sicher, damit Builds direkt in VS Code ohne globale Maven-Installation funktionieren.

### 3.2 Visual Theme & Editor (IntelliJ Style)
In der `.vscode/settings.json` werden folgende "State-of-the-Art" Einstellungen gesetzt:
* **Theme:** Optimiert für "Default Dark Modern" (IntelliJ Dark ähnlich).
* **Schriftart:** Nutzung von **'JetBrains Mono'** mit aktivierten Ligaturen für beste Code-Lesbarkeit.
* **UI:** Minimap deaktiviert, Zeilenhöhe auf 1.5 für einen sauberen Look.

---

## 4. Technischer RAG-Workflow
Das System implementiert den **Advanced RAG Workflow**:
1.  **Retrieval:** Ein Query-Embedding wird erzeugt. Der `VectorSearchAdapter` führt eine Cosine-Similarity Suche in der `knowledge_base` durch.
2.  **Filtering:** Der `ContentSafetyService` entfernt Treffer, die nicht zur Persona-Policy passen (z.B. NSFW-Filter für Support-Bots).
3.  **Generation:** Die Persona generiert die Antwort basierend auf ihrem `exampleDialog` (Few-Shot) und dem `promptTemplate`.

---

## 5. Quick Start
1.  **Setup ausführen:** `./src/setup.ps1` (Bereinigt Cache, setzt VS Code Settings).
2.  **Profile wählen:**
    * `--spring.profiles.active=school`: Lokal mit H2 In-Memory DB.
    * `--spring.profiles.active=home`: Cloud mit Neon PostgreSQL & Vektor-Suche.
3.  **Build:** `./mvnw clean compile` (oder F5 in VS Code dank `launch.json`).

---
*Entwickelt von Christian Langner | Fokus auf Enterprise-Architektur und KI-Integration.*
