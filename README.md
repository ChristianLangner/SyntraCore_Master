# SyntraCore Development Repository

## 📋 Projekt-Übersicht

SyntraCore ist ein Spring-Boot-basiertes Backend-System, das nach den Prinzipien der **Hexagonalen Architektur** (auch bekannt als Ports & Adapters) entwickelt wird. Das Projekt ist aktuell in der ersten Entwicklungsphase und fokussiert sich auf die Grundlagen: Domain-Modell, Datenbankanbindung und die Verbindung zwischen beiden Schichten.

## 🏗️ Architektur

Das Projekt folgt der **Hexagonalen Architektur** (Ports & Adapters), die eine klare Trennung zwischen Business-Logik und technischer Infrastruktur ermöglicht:

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                     │
│              (SyntraCoreApplication)                     │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
│  ┌──────────────────┐      ┌──────────────────────┐     │
│  │  SupportTicket   │      │ TicketRepositoryPort │     │
│  │  (Domain Model)  │      │    (Port Interface)  │     │
│  └──────────────────┘      └──────────────────────┘     │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Adapter Layer                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │     TicketDatabaseAdapter                        │   │
│  │     (implementiert TicketRepositoryPort)         │   │
│  └──────────────────────────────────────────────────┘   │
│                         │                                │
│                         ▼                                │
│  ┌──────────────────────────────────────────────────┐   │
│  │     SpringDataTicketRepository                   │   │
│  │     TicketJpaEntity                              │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Database (H2 In-Memory)               │
└─────────────────────────────────────────────────────────┘
```

### Schichten-Erklärung:

1. **Domain Layer** (`core/domain`, `core/ports`)
   - Enthält die reine Business-Logik ohne Framework-Abhängigkeiten
   - `SupportTicket`: Domain-Modell für ein Support-Ticket
   - `TicketRepositoryPort`: Interface (Port) für das Speichern von Tickets

2. **Adapter Layer** (`adapters/outbound/database`)
   - Verbindet das Domain-Layer mit der technischen Infrastruktur
   - `TicketDatabaseAdapter`: Implementiert das Port-Interface und übersetzt Domain-Objekte in JPA-Entitäten
   - `TicketJpaEntity`: JPA-Entität für die Datenbank
   - `SpringDataTicketRepository`: Spring Data Repository für Datenbankoperationen

3. **Application Layer**
   - `SyntraCoreApplication`: Startet die Anwendung und führt einen Herzschlag-Test durch

## 🛠️ Technologie-Stack

- **Java 21**: Programmiersprache
- **Spring Boot 3.2.3**: Framework für Enterprise-Anwendungen
- **Spring Data JPA**: Vereinfachter Datenbankzugriff
- **H2 Database**: In-Memory-Datenbank für Entwicklung
- **Lombok**: Reduziert Boilerplate-Code (Getter/Setter)
- **Maven**: Build-Tool und Dependency-Management

## ✅ Aktueller Stand (Step 1: Backend & Database)

### Was ist fertig:

- ✅ **Domain-Modell**: `SupportTicket` Klasse mit Business-Logik
- ✅ **Port-Interface**: `TicketRepositoryPort` für Repository-Operationen
- ✅ **Datenbank-Adapter**: `TicketDatabaseAdapter` verbindet Domain mit DB
- ✅ **JPA-Entität**: `TicketJpaEntity` für Datenbank-Persistierung
- ✅ **Spring Data Repository**: `SpringDataTicketRepository` für CRUD-Operationen
- ✅ **H2-Datenbank**: Konfiguriert und läuft im In-Memory-Modus
- ✅ **Herzschlag-Test**: Automatischer Test beim Start, der die Verbindung prüft
- ✅ **Alle Dateien kommentiert**: Ausführliche JavaDoc- und Inline-Kommentare

### Was noch kommt:

- 🔲 REST-API Endpunkte (Controller)
- 🔲 Telegram-Bot Integration
- 🔲 Use Cases / Service Layer
- 🔲 Validierung und Fehlerbehandlung
- 🔲 Tests (Unit & Integration)
- 🔲 PostgreSQL für Produktion

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
2. **Use Cases** implementieren (z.B. `CreateTicketUseCase`)
3. **Telegram-Bot** integrieren
4. **Validierung** hinzufügen
5. **Tests** schreiben

---

**Status**: 🟢 Step 1 abgeschlossen - Backend & Database Grundlagen sind implementiert und getestet.
