# 🚀 AyntraCore – Quick Start Setup (Credentials Edition)

**Ziel:** Erste lokale Installation mit echten Datenbankverbindungen in 10 Minuten.

---

## 1️⃣ Voraussetzungen

- ✅ Java 21 installiert
- ✅ Maven 3.9+ oder `./mvnw`
- ✅ Git Repository geklont
- ❌ Keine .env Datei (wird neu erstellt!)

---

## 2️⃣ Neon PostgreSQL Setup (5 Min)

### Schritt A: Neon Account & Projekt

```bash
# 1. Öffne Browser: https://console.neon.tech
# 2. Sign Up (kostenlos)
# 3. Verify Email
# 4. "New Project"
#    - Name: AyntraCore
#    - Region: EU (deine Region)
#    - PostgreSQL: Latest
# 5. Wait for initialization...
```

### Schritt B: Connection String kopieren

Nach der Erstellung siehst du einen **"Connection string (Pooler)"**:

```
postgresql://neondb_owner:npg_4LDepmQGJbT7@ep-raspy-bird-agdreweq-pooler.c-2.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require
```

**SPEICHERN** (brauchen wir gleich!)

---

## 3️⃣ OpenRouter API Key Setup (3 Min)

### Schritt A: OpenRouter Account

```bash
# 1. Öffne: https://openrouter.ai
# 2. Sign Up (Email oder GitHub)
# 3. Verify Email
# 4. Settings → Billing → Add Payment Method (Kreditkarte)
# 5. Budget Limit (optional, z.B. $10/Monat)
```

### Schritt B: API Key generieren

```bash
# 1. Settings → Keys
# 2. "Create Key"
# 3. Name: "AyntraCore-Dev"
# 4. Copy den langen Key (beginnt mit sk-or-v1-...)
#
# Beispiel: sk-or-v1-abc123def456...
```

**SPEICHERN** (brauchen wir als nächstes!)

---

## 4️⃣ Lokale .env-Datei erstellen (2 Min)

### Terminal:

```bash
# Im Projektroot:
cd /workspaces/AyntraCore_Master

# Kopiere Template:
cp .env.template .env

# Öffne im Editor:
code .env  # VS Code
# oder: nano .env, vim .env, etc.
```

### Eintragen:

Ersetze die Platzhalter mit DEINEN echten Werten:

```env
# 1. DATABASE_URL – von Neon kopiert
DATABASE_URL=postgresql://neondb_owner:npg_4LDepmQGJbT7@ep-raspy-bird-agdreweq-pooler.c-2.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require

# 2. OPENROUTER_API_KEY – von openrouter.ai kopiert
OPENROUTER_API_KEY=sk-or-v1-abc123def456ghijklmnop

# 3. Profile
SPRING_PROFILES_ACTIVE=home

# 4. Server
SERVER_PORT=8080
```

### Speichern & sichern:

```bash
# Datei speichern (CRUCIAL!)

# Optional, aber Sicherheit:
chmod 600 .env  # Nur du kannst lesen

# Überprüfe, dass .env im .gitignore ist:
grep "\.env" .gitignore  # Sollte "✅ Treffer" geben
```

---

## 5️⃣ Anwendung starten (nicht lange!)

### Option A: Maven

```bash
# Mit Java 21:
export JAVA_HOME=/usr/local/sdkman/candidates/java/21.0.9-ms
export PATH=$JAVA_HOME/bin:$PATH

# Build:
mvn clean compile -DskipTests

# Start:
mvn spring-boot:run
```

**Expected Output:**
```
[INFO] AyntraCore started on port 8080 with profiles: [home]
[INFO] Database: postgresql://neondb_owner:***@...neon.tech:5432/neondb
```

### Option B: VS Code Debugger

```bash
# 1. F5 drücken (oder Debugger Symbol)
# 2. Wähle Profil: "AyntraCore: HOME (Neon PostgreSQL)"
# 3. Breakpoint setzen (optional)
# 4. App startet mit allen .env Credentials
```

### Option C: Docker (Bonus)

```bash
# Build Image mit .env:
docker build -t ayntracore:dev .

# Run mit Credentials:
docker run --env-file .env -p 8080:8080 ayntracore:dev
```

---

## ✅ Überprüfung

```bash
# 1. App läuft auf Port 8080:
curl http://localhost:8080/actuator/health

# Expected:
# {"status":"UP"}

# 2. Database verbunden:
curl http://localhost:8080/actuator/db

# Expected:
# Database is running

# 3. Logs zeigen kein Error:
# [INFO] Spring Boot Application started Successfully
```

---

## 🚨 Häufige Fehler

| Fehler | Grund | Lösung |
| --- | --- | --- |
| `Failed to initialize pool: Connection refused` | DB nicht erreichbar | Überprüfe DATABASE_URL Spelling & Neon Status |
| `Unable to read model: Received non-all-whitespace CHARACTERS` | Fehler in .env | Entferne alte .env, kopiere neu von .env.template |
| `SCRAM authentication failed` | Falscher Host | Nutze `-pooler` Host, nicht Discord-Host |
| `OpenRouter 401 Unauthorized` | API Key falsch | Überprüfe sk-or-v1- Prefix & kopiere exact |
| `.env wird nicht geladen` | Spring Boot findet Datei nicht | Stelle sicher .env im ROOT ist (`ls -la .env`) |

---

## 🔒 Security Reminder

```bash
# ✅ TU DAS:
echo ".env" >> .gitignore         # Schütze Credentials
chmod 600 .env                    # Nur du kannst lesen
git add CREDENTIALS.md            # Dokumentation IS OK!

# ❌ TU DAS NICHT:
git add .env                      # 🚨 FALSCH – Credentials werden sichtbar
git commit -m "Add API key"       # 🚨 FALSCH – In History sichtbar
cat .env | slack                  # 🚨 FALSCH – Mit Team teilen
```

---

## 📚 Weitere Dokumentation

- **CREDENTIALS.md** – Detaillierte Production Setup & Secrets Management
- **README.md** – Architektur & Konzepte
- **application-home.properties** – Spring Boot Production Config
- **application-school.properties** – Spring Boot Development Config (H2)

---

## 🎯 Nächste Schritte

1. ✅ .env erstellt & Credentials eingetragen
2. ✅ Anwendung startet erfolgreich
3. 🔜 Persönalisiere Persona (Chat-Charakter) in Admin UI
4. 🔜 Lade Knowledge Base mit deinen Daten
5. 🔜 Deploy zu Production (siehe CREDENTIALS.md → Production Deployment)

---

**Viel Erfolg! 🚀**

Fragen? → Siehe CREDENTIALS.md oder README.md Details
