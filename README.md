# SyntraCore Development Repository

## 📋 Projektübersicht

**SyntraCore** ist ein KI-gestütztes Support-System, das auf **Spring Boot (v3.2.3)** basiert und nach den Prinzipien der **Hexagonalen Architektur** (Ports & Adapters) entwickelt wurde. Das System nutzt **RAG (Retrieval-Augmented Generation)**, um Anfragen mit spezifischem Wissen zu beantworten.

Aktuell ist SyntraCore in der **Phase 2** und konzentriert sich auf folgende Kernbereiche:
- Domain-Modellierung und **Business-Logik**
- **Resiliente Infrastruktur**, die lokal und in der Cloud funktioniert
- Echtzeit-KI **RAG-basierte Analyse**
- Integration mit Datenbanken und externen KI-Diensten

---

## 🏗️ Architektur und Design

SyntraCore folgt der **Hexagonalen Architektur**, die eine klare Trennung zwischen der Geschäftslogik und technischer Infrastruktur sicherstellt. Die Hauptbestandteile der Architektur sind:

### 🔹 Schichtendiagramm:
```plaintext
┌──────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
│               (SyntraCoreApplication.java)                  │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│   (TicketService, orchestriert KI-Integration & DB)          │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│ (SupportTicket und Ports wie TicketRepositoryPort, AiPort)   │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  Adapter Layer                               │
│ - Database Adapter: JPA Entitäten + Repository               │
│ - KI Adapter: OpenAI Integration für RAG                    │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│              Datenbanken & externe KI-Dienste               │
└──────────────────────────────────────────────────────────────┘
```

### 🔹 Schichtenerklärung:

1. **Application Layer**:
    - Einstiegspunkt der Anwendung (`SyntraCoreApplication`) und Basis-Setup.

2. **Service Layer** (Orchestrierung):
    - Enthält Geschäftslogik, um Abläufe wie das Anlegen eines Tickets und die KI-Analyse zu koordinieren.

3. **Domain Layer**:
    - **Business-Logik** und zentrale Schnittstellen:
        - `SupportTicket`: Zentrales Modell für Tickets.
        - `TicketRepositoryPort`: Schnittstelle für Datenbankspeicherungen.
        - `AiServicePort`: Schnittstelle für KI-Analysen.

4. **Adapter Layer**:
    - Bindeglied zu externen Systemen:
        - **Datenbank-Adapter**: Verbindet JPA mit dem Domain-Modell (`TicketDatabaseAdapter`).
        - **KI-Adapter**: Implementiert das `AiServicePort` Interface (z. B. OpenAI).

---

## 🛠️ Technologie-Stack

- **Programmiersprache**: Java 21
- **Framework**: Spring Boot 3.2.3
- **Datenbank**: H2 (lokal) bzw. PostgreSQL (Cloud)
- **KI-Integration**: OpenAI über REST-Adapter
- **Persistenz**: Spring Data JPA
- **Web-Framework**: WebSocket (STOMP) und REST
- **Build-Tool**: Maven
- **Weitere Tools**: Lombok zur Reduzierung von Boilerplate Code

---

## 🚀 Setup und Start

### Voraussetzungen:

- **Java 21** oder neuer
- **Maven 3.6+**
- **IDE**: IntelliJ IDEA (empfohlen)

### Schritte:

1. **Code klonen**:
   ```bash
   git clone <REPOSITORY_URL>
   cd SyntraCore_Master
   ```

2. **Maven-Dependencys installieren**:
   ```bash
   mvn clean install
   ```

3. **Profil konfigurieren**:
    - In der Datei `application.properties` können Sie das Profil setzen:
      ```properties
      spring.profiles.active=school # oder 'home'
      ```

4. **Anwendung starten**:
    - Via IntelliJ IDEA:
        - Rechtsklick auf `SyntraCoreApplication.java` → "Run".
    - Oder über Maven:
      ```bash
      mvn spring-boot:run
      ```

5. **Zugriffspunkte nach Start**:
    - **REST API Base URL**: `http://localhost:8080`
    - **H2 Konsole**: `http://localhost:8080/h2-console`
        - Datenbank-URL: `jdbc:h2:mem:syntracoredb`
        - Benutzername: `sa`, Passwort: leer

---

## 📁 Projektstruktur

```plaintext
src/main/java/com/syntracore/
├── SyntraCoreApplication.java          # Hauptklasse
├── core/
│   ├── domain/
│   │   └── SupportTicket.java          # Domain-Modell
│   └── ports/
│       └── TicketRepositoryPort.java   # Schnittstellendefinition
└── adapters/
    ├── outbound/
    │   ├── TicketDatabaseAdapter.java      # Adapter: Domain ↔ Datenbank
    │   ├── TicketJpaEntity.java            # JPA-Entität
    │   └── SpringDataTicketRepository.java # Spring Data Repository
    └── inbound/
        └── TicketController.java           # REST-API Endpunkt

src/main/resources/
└── application.properties               # App-Konfiguration
```

---

## ✅ Aktueller Fortschritt

### Fertiggestellt:
- **Domain-Modelle**: `SupportTicket` inkl. KI-Analysefeld
- **Port-Interfaces**: `TicketRepositoryPort`, `AiServicePort`
- **Service-Layer**: `TicketService` mit KI-orchestrierten Workflows
- **Adapter**:
    - `TicketDatabaseAdapter` (für Datenbankoperationen)
    - `OpenAiAdapter` (simulierte KI-Integration)
- **H2 Datenbank**: Voll einsatzbereit (In-Memory-Modus)
- **REST-API**: Erste Endpunkte implementiert (`TicketController`)

### Geplant:
- **Telegram-Bot**: Live-Support über Messaging
- **Erweiterte Datenbankkonzepte**:
    - Vektordatenbank (z. B. Pinecone/Weaviate)
    - PostgreSQL als Produktivdatenbank
- **Performance-Optimierungen**:
    - Asynchrone Kommunikation über Message Queues
    - Verbesserte KI-Integration mit echtem Deployment
- **Monitoring**: Anbindung an Prometheus und Grafana
- **Automatisiertes Testing**:
    - Unit-Tests (Service-Layer)
    - Integrationstests (Adapter + Ports)

---

## 💡 Nächste Schritte

1. **Telegram-Integration**:
    - Inbound Adapter für Nutzerkommunikation.
2. **Echte KI-Anbindung**:
    - OpenAI statt simulierte Antworten.
3. **Erweiterungen**:
    - Statusmanagement und Ticket-Priorisierung.
4. **Cloud-Ready machen**:
    - Migration zu PostgreSQL.
5. **Automatisiertes Testing**:
    - Vollständige Test-Abdeckung.

---

Wenn Sie weitere Details benötigen oder gezielte Anpassungen wünschen, lassen Sie es mich wissen! 😊