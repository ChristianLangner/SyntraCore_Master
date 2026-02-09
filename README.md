# 🚀 AyntraCore – Enterprise Multi-Tenant Persona & RAG Engine

**Architektur-Paradigma:** Hexagonal (Ports & Adapters)  
**Entwicklungs-Status:** Phase 3.0 (Resilience, Multi-Tenancy & Vector-Search)  
**Technologie-Stack:** Java 21, Spring Boot 3.2.3, PostgreSQL (pgvector)

---

## 1. Architektur-Dokumentation (IHK-Prüfungsniveau)

AyntraCore folgt einer strikten **Hexagonalen Architektur**. Das primäre Ziel ist die vollständige Entkopplung der Geschäftslogik von technischen Infrastruktur-Details wie Datenbanken, Frameworks oder spezifischen KI-Providern.

### 1.1 Schichtenmodell & Separation of Concerns
* **Domain Layer (Hexagonal Core):** Beinhaltet die fachlichen Entitäten (`Persona`, `SupportTicket`, `Knowledge`) und Kern-Logik. Diese Schicht ist "rein" und besitzt keine Abhängigkeiten zu Spring oder JPA.
* **Application Layer:** Implementiert die Use-Cases. Hier findet die Orchestrierung statt (z. B. der RAG-Workflow im `RAGCoordinationService`).
* **Infrastructure Layer (Adapters):** * **Inbound-Adapter:** Web-Controller (`AdminController`) und WebSocket-Endpunkte (`ChatController`) für die Außenkommunikation.
    * **Outbound-Adapter:** Realisierung der Persistenz (`VectorSearchAdapter`) und KI-Kommunikation (`OpenAiAdapter`).

### 1.2 Verwendete Entwurfsmuster
* **Strategy Pattern:** Dynamische Auswahl des KI-Modells und Such-Algorithmus basierend auf Spring-Profilen (`home` vs. `school`).
* **Factory Pattern:** Zentralisierte Instanziierung der KI-Provider über die `AiProviderFactory`.
* **Repository Pattern:** Abstraktion der Datenquelle durch dedizierte Output-Ports.

---

## 2. Deep Dive: Schlüsselkomponenten & Klassenreferenz
*Diese Sektion ermöglicht es KI-Agenten und Senior-Entwicklern, die Systemmechanik sofort zu erfassen.*

### 🧠 Core-Logik (Domain & Application)
| Klasse | Pfad | Funktion & Business Impact |
| :--- | :--- | :--- |
| `Persona` | `core.domain` | **Das digitale Ich:** Definiert Identität, Sprachstil und dynamische JSON-Traits. Steuert das Verhalten des Bots via `promptTemplate`. |
| `Knowledge` | `core.domain` | **Informations-Vektor:** Hält Rohdaten und deren 1536-dimensionale Embeddings für die semantische Suche. |
| `RAGCoordinationService` | `core.application` | **Orchestrator:** Steuert den RAG-Dreiklang: Retrieval (Vektorsuche) -> Augmentation (Kontextanreicherung) -> Generation (KI-Antwort). |
| `ContentSafetyService` | `core.application` | **Gatekeeper:** Validiert User-Inputs gegen Prompt-Injection und filtert explizite Inhalte basierend auf der Persona-Policy. |

### 🔌 Infrastruktur (Adapters)
| Klasse | Pfad | Technische Implementierung |
| :--- | :--- | :--- |
| `VectorSearchAdapter` | `adapters.outbound` | **Semantic Engine:** Implementiert die Vektorsuche mittels `pgvector` und Cosine Distance (`<=>`) in PostgreSQL. |
| `OpenAiAdapter` | `adapters.outbound` | **AI-Gateway:** Übersetzt Domain-Requests in OpenAI-kompatible Payloads für OpenRouter (DeepSeek/GPT). |
| `ChatController` | `adapters.inbound` | **Real-Time Port:** Verwaltet asynchrone WebSocket-Verbindungen (STOMP) für den Live-Support. |

---

## 3. VS Code "IntelliJ-Experience" & Setup

Das Projekt ist für eine nahtlose Entwicklung in VS Code optimiert, um die gewohnte IntelliJ-Produktivität zu erreichen.

### 3.1 Automatisierter Build-Prozess
Das mitgelieferte `setup.ps1` konfiguriert den Workspace so, dass die `pom.xml` als Single-Source-of-Truth dient:
* **Automatische Indizierung:** Maven-Abhängigkeiten werden sofort erkannt.
* **Maven Wrapper:** Nutzung von `./mvnw` stellt sicher, dass keine lokale Maven-Installation nötig ist.

### 3.2 Visual & Editor Settings (State-of-the-Art)
Die `.vscode/settings.json` erzwingt ein professionelles Erscheinungsbild:
* **Theme:** `Default Dark Modern` (optimierte Kontraste für lange Sessions).
* **Font:** **'JetBrains Mono'** mit aktivierten Ligaturen für kristallklare Code-Symbole.
* **UI-Cleanliness:** Minimap deaktiviert, Zeilenhöhe auf 1.5 für reduzierte kognitive Last.

---

## 4. Multi-Tenancy & RAG-Workflow
Das System implementiert eine strikte **Mandantenfähigkeit**:
1.  **Isolation:** Alle Daten (Tickets, Wissen, Personas) sind über eine `companyId` (UUID) getrennt.
2.  **RAG-Prozess:** * **Retrieval:** Suche relevanter Wissenssegmente via Vektor-Ähnlichkeit.
    * **Augmentation:** Dynamische Injektion des Wissens in den System-Prompt der Persona.
    * **Safety:** Finale Prüfung des generierten Contents vor der Auslieferung.

---

## 5. Quick Start
1.  **Initialisierung:** `./src/setup.ps1` ausführen (reinigt Cache & setzt IDE-Settings).
2.  **API-Konfiguration:** `OPENROUTER_API_KEY` in der Umgebung setzen.
3.  **Start (Cloud/Home):** ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=home
    ```

---
*Autor: Christian Langner | Architektur-Version: 3.0 (AyntraCore DNA)*
