# 🚀 SyntraCore – Intelligent Support Engine

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-blue?style=for-the-badge)](https://en.wikipedia.org/wiki/Hexagonal_architecture_(software))

**SyntraCore** ist ein KI-gestütztes Support-System auf Basis von **Java 21** und **Spring Boot 3.2.x**.  
Das System nutzt **RAG (Retrieval-Augmented Generation)**, um Support-Anfragen präzise, kontextbewusst und im Stil definierbarer Persönlichkeiten (**Personas**) zu beantworten.

---

## 📋 Inhaltsverzeichnis

---

## ✨ Kernfeatures

### 💬 Live-Chat & KI-Interaktion
- **Echtzeit-Support:** WebSockets (STOMP/SockJS) für eine flüssige User Experience.
- **RAG-Engine:** Kontextbasierte Wissensanreicherung, damit die KI nur relevante Fakten erhält.
- **Persona Preview:** Live-UI zeigt Persona-Name/Stil und optional ein Avatar (Trait `avatarUrl`).

### 🛠 Admin-Panel (Management-Zentrale)
- **Knowledge Ingest:** Pflege der Wissensbasis über die Admin-Oberfläche.
- **Ticket Management:** Übersicht über offene Tickets + Status-Handling (Resolve).
- **Persona Studio:** Konfiguration von Prompt-Templates, Traits (JSON) und optionalen Beispieldialogen.

### 🧩 KI-Adapter (OpenRouter)
- Austauschbare KI-Anbindung über Port/Adapter.
- Sauberes Fallback-Verhalten, wenn kein API-Key gesetzt ist (Demo-freundlich).

Aktuell ist SyntraCore in der **Phase 2** und konzentriert sich auf folgende Kernbereiche:
- Domain-Modellierung und **Business-Logik**
- **Resiliente Infrastruktur**, die lokal und in der Cloud funktioniert
- Echtzeit-KI **RAG-basierte Analyse**
- Integration mit Datenbanken und externen KI-Diensten

---

## 🏗 Architektur & Design

SyntraCore ist nach der **Hexagonalen Architektur (Ports & Adapters)** strukturiert.  
Ziel: **Fachlogik** (Core) bleibt unabhängig von **Frameworks** und **Infrastruktur**.

### Schichtentrennung
- **Core (Domain & Services):** Reine Geschäftslogik + Port-Definitionen (Schnittstellen).
- **Inbound Adapters:** Web (Thymeleaf/Admin) + WebSocket (Live-Chat).
- **Outbound Adapters:** Persistenz (H2/PostgreSQL via Adapter) + KI-Kommunikation (OpenRouter).

---

## 🛠 Technologie-Stack

| Komponente | Technologie |
| :--- | :--- |
| **Sprache** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.2.x |
| **Frontend/Web** | Thymeleaf, HTML5, JavaScript (SockJS, STOMP) |
| **KI-Integration** | OpenRouter API (Chat Completions) |
| **Datenbank** | H2 (In-Memory) / PostgreSQL |
| **Build-Management** | Maven |

---

## 📁 Projektstruktur

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

## 🚀 Setup & Start

### Voraussetzungen
- Java 21+
- Maven 3.6+
- (Optional) OpenRouter API-Key (sonst liefert das System einen verständlichen Hinweis “KI nicht konfiguriert”)

### Installation
Repository klonen: