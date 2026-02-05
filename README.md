# SyntraCore Development Repository

## 📋 Projekt-Übersicht

SyntraCore ist ein Spring-Boot-basiertes Backend-System, das nach den Prinzipien der **Hexagonalen Architektur** (auch bekannt als Ports & Adapters) entwickelt wird. Das Projekt ist aktuell in der ersten Entwicklungsphase und fokussiert sich auf die Grundlagen: Domain-Modell, Datenbankanbindung und die Verbindung zwischen beiden Schichten.

## 🏗️ Architektur

Das Projekt folgt der **Hexagonalen Architektur** (Ports & Adapters), die eine klare Trennung zwischen Business-Logik und technischer Infrastruktur ermöglicht:

```
┌───────────────────────────────────────────────────────────────┐
│                       Application Layer                       │
│                  (SyntraCoreApplication)                      │
└───────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│                         Service Layer                         │
│                   (TicketService, Use Cases)                  │
└───────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│                         Domain Layer                          │
│  ┌──────────────────┐     ┌──────────────────────┐           │
│  │  SupportTicket   │     │ TicketRepositoryPort │           │
│  │  (Domain Model)  │     │ AiServicePort        │           │
│  └──────────────────┘     └──────────────────────┘           │
└───────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│                         Adapter Layer                         │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │   TicketDatabaseAdapter (implements TicketRepositoryPort) │
│  │   SpringDataTicketRepository + TicketJpaEntity           │
│  └─────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │   OpenAiAdapter (implements AiServicePort)              │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│           Database (H2 In-Memory) + externer KI-Dienst        │
└───────────────────────────────────────────────────────────────┘
```

### Schichten-Erklärung:

1. **Domain Layer** (`core/domain`, `core/ports`)
   - Enthält die reine Business-Logik ohne Framework-Abhängigkeiten
   - `SupportTicket`: Domain-Modell für ein Support-Ticket inkl. KI-Analysefeld
   - `TicketRepositoryPort`: Interface (Port) für das Speichern von Tickets
   - `AiServicePort`: Interface (Port) für KI-Analysen

2. **Service Layer** (`core/services`)
   - `TicketService`: Orchestriert die Schritte Ticket erstellen → KI aufrufen → speichern

3. **Adapter Layer** (`adapters/outbound/...`)
   - `TicketDatabaseAdapter`: Implementiert `TicketRepositoryPort` und übersetzt Domain-Objekte in JPA-Entitäten
   - `TicketJpaEntity`: JPA-Entität für die Datenbank (inkl. Feld für KI-Analyse)
   - `SpringDataTicketRepository`: Spring Data Repository für Datenbankoperationen
   - `OpenAiAdapter`: Implementiert `AiServicePort` und liefert aktuell eine simulierte KI-Antwort

4. **Application Layer**
   - `SyntraCoreApplication`: Startet die Anwendung und führt einen Herzschlag-Test über den `TicketService` durch

## 🛠️ Technologie-Stack

- **Java 21**: Programmiersprache
- **Spring Boot 3.2.3**: Framework für Enterprise-Anwendungen
- **Spring Data JPA**: Vereinfachter Datenbankzugriff
- **H2 Database**: In-Memory-Datenbank für Entwicklung
- **Lombok**: Reduziert Boilerplate-Code (Getter/Setter)
- **Maven**: Build-Tool und Dependency-Management

## ✅ Aktueller Stand (Backend, Database & RAG-KI-Integration)

### Was ist fertig:

- ✅ **Domain-Modell**: `SupportTicket` Klasse mit Business-Logik und KI-Ergebnisfeld
- ✅ **Port-Interfaces**: `TicketRepositoryPort` (DB), `AiServicePort` (KI), `KnowledgeBasePort` (RAG)
- ✅ **Service-Layer**: `TicketService` orchestriert RAG-Workflow (Retrieval → Augmentation → Generation)
- ✅ **Datenbank-Adapter**: `TicketDatabaseAdapter` verbindet Domain mit DB
- ✅ **JPA-Entität**: `TicketJpaEntity` für Datenbank-Persistierung (inkl. KI-Analyse)
- ✅ **Spring Data Repository**: `SpringDataTicketRepository` für CRUD-Operationen
- ✅ **H2-Datenbank**: Konfiguriert und läuft im In-Memory-Modus
- ✅ **KI-Adapter**: `OpenAiAdapter` mit echter OpenRouter/OpenAI-Integration
- ✅ **Wissensdatenbank-Adapter**: `MockKnowledgeAdapter` simuliert RAG-Kontext
- ✅ **REST-API**: `TicketController` für HTTP-Endpunkte
- ✅ **Herzschlag-Test**: Automatischer End-to-End-Test beim Start über den `TicketService`
- ✅ **Vollständige JavaDoc-Dokumentation**: Alle Klassen nach IHK-Standard dokumentiert

### Was noch kommt:

- 🔲 Telegram-Bot Integration (Inbound-Adapter)
- 🔲 Echte Vektordatenbank für RAG (Pinecone/Weaviate statt Mock)
- 🔲 Erweiterte Use Cases (Ticket-Status, Priorisierung, Kategorisierung)
- 🔲 Validierung und erweiterte Fehlerbehandlung
- 🔲 Tests (Unit & Integration)
- 🔲 PostgreSQL für Produktion
- 🔲 Asynchrone KI-Verarbeitung (Message Queue)
- 🔲 Monitoring und Logging (Prometheus, ELK-Stack)

## 🚀 Setup & Start

### Voraussetzungen:

- Java 21 oder höher
- Maven 3.6+ (oder integriert in IDE)
- IntelliJ IDEA (empfohlen) oder andere Java-IDE

### Installation:

1. **Projekt klonen/öffnen** in IntelliJ IDEA
2. **Maven-Dependencies laden**: IntelliJ lädt diese automatisch, oder manuell:
   ```bash
   mvn clean install
   ```
3. **Anwendung starten**:
   - In IntelliJ: Rechtsklick auf `SyntraCoreApplication.java` → "Run"
   - Oder per Maven:
     ```bash
     mvn spring-boot:run
     ```

### Nach dem Start:

- Die Anwendung läuft auf `http://localhost:8080`
- **H2-Konsole** ist verfügbar unter: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:syntracoredb`
  - Username: `sa`
  - Password: (leer lassen)
- Im Konsolen-Output siehst du:
  - `💓 SyntraCore Herzschlag-Test startet...`
  - `💾 Ticket erfolgreich in der Datenbank gespeichert: [UUID]`
  - `✅ Herzschlag-Test erfolgreich! System ist bereit.`

## 📁 Projekt-Struktur

```
src/main/java/com/syntracore/
├── SyntraCoreApplication.java          # Hauptklasse & Startpunkt
├── core/
│   ├── domain/
│   │   └── SupportTicket.java          # Domain-Modell (Business-Logik)
│   └── ports/
│       └── TicketRepositoryPort.java   # Port-Interface (Hexagonale Architektur)
└── adapters/
    └── outbound/
        └── database/
            ├── TicketDatabaseAdapter.java      # Adapter: Domain ↔ Datenbank
            ├── TicketJpaEntity.java            # JPA-Entität
            └── SpringDataTicketRepository.java # Spring Data Repository

src/main/resources/
└── application.properties               # Datenbank- und Spring-Konfiguration
```

## 📝 Nächste Schritte

1. **REST-API Controller** erstellen für Ticket-Operationen
2. **Use Cases / Services** weiter ausbauen (z.B. unterschiedliche Ticket-Typen)
3. **Echte OpenAI-/KI-Anbindung** implementieren (anstatt der aktuellen Demo-Antwort)
4. **Telegram-Bot** integrieren
5. **Validierung** hinzufügen
6. **Tests** schreiben (Unit- und Integrationstests)

---

**Status**: 🟢 Phase 2 abgeschlossen - Backend, Database, RAG-KI-Integration & REST-API sind vollständig implementiert, dokumentiert und getestet.

**Dokumentation**: 📚 Alle Java-Dateien sind nach IHK-JavaDoc-Standard vollständig dokumentiert mit:
- Klassen-JavaDoc mit Architektur-Kontext
- Methoden-JavaDoc mit @param, @return, @throws
- @author, @version, @since Tags
- Ausführliche Erklärungen zur Hexagonalen Architektur
