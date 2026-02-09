# 🔐 AyntraCore – Credentials & Secrets Management

**Zielgruppe:** Developer, DevOps, IHK-Kandidaten  
**Version:** 3.0 | Multi-Tenant Production-Ready  

---

## 📖 Inhaltsverzeichnis
1. [Lokale Development-Umgebung](#lokale-development-umgebung)
2. [Production Deployment](#production-deployment)
3. [Neon PostgreSQL Setup](#neon-postgresql-setup)
4. [OpenRouter API Setup](#openrouter-api-setup)
5. [Security Best Practices](#security-best-practices)

---

## 🚀 Lokale Development-Umgebung

### Schritt 1: .env-Datei erstellen

```bash
# Im Projektroot:
cp .env.example .env

# Dann mit deinem Editor öffnen und Werte eintragen
code .env  # oder: nano .env
```

### Schritt 2: Credentials eintragen

#### **A) Neon PostgreSQL (Production Profile: `home`)**

Deine Credentials findest du im **Neon Dashboard**:

1. Gehe zu https://console.neon.tech
2. Projekt auswählen → Database
3. Kopiere die **Connection String** (mit Pooler!)

```env
# Beispiel (NICHT REAL – verwende deine echten Werte!)
DATABASE_URL=postgresql://neondb_owner:npg_4LDepmQGJbT7@ep-raspy-bird-agdreweq-pooler.c-2.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require
```

**Format verstehen:**
```
postgresql://
  ↓ User
  neondb_owner:
    ↓ Password
    npg_4LDepmQGJbT7@
      ↓ Pooler-Host (WICHTIG für Connection-Limits!)
      ep-raspy-bird-agdreweq-pooler.c-2.eu-central-1.aws.neon.tech:5432/
        ↓ Database
        neondb?
          ↓ SSL-Modus
          sslmode=require
```

**⚠️ WICHTIG: Nutze den POOLER-Host, nicht den Auto-Suspend Host!**

#### **B) OpenRouter API Key (KI-Service)**

1. Gehe zu https://openrouter.ai/keys
2. Melde dich an / Registriere dich
3. Klicke "Create Key"
4. Name: `AyntraCore-Dev` (oder ähnlich)
5. Kopiere den Key

```env
# Beispiel (NICHT REAL!)
OPENROUTER_API_KEY=sk-or-v1-ab12cd34ef56gh78ij90kl12mn34op56qr78st90
```

#### **C) Spring Profile (lokal: welche DB?)**

```env
# Für Neon (Production-ähnlich):
SPRING_PROFILES_ACTIVE=home

# Oder für H2 In-Memory (schnell & offline):
SPRING_PROFILES_ACTIVE=school
```

### Schritt 3: Anwendung starten

```bash
# Mit der .env-Datei (Spring Boot liest sie automatisch via spring-dotenv)
export JAVA_HOME=/usr/local/sdkman/candidates/java/21.0.9-ms
mvn spring-boot:run

# Oder in VS Code: F5 (nutzt launch.json)
```

**Überprüfe, dass die Credentials geladen wurden:**
```bash
# In der Console sollte so etwas stehen:
# [INFO] Application 'ayntracore' started on port 8080 with profile 'home'
# [INFO] Datasource URL: postgresql://neondb_owner:***@...neon.tech:5432/neondb
```

---

## 🏭 Production Deployment

### **Umgebung A: Docker / Containerisierung**

Credentials werden als **Docker Environment Variables** übergeben:

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY target/ayntracore-0.0.1-SNAPSHOT.jar app.jar

# Entrypoint: Keine .env-Datei! Nur Environment Vars
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Start mit Secrets:
```bash
docker run -e DATABASE_URL="postgresql://..." \
           -e OPENROUTER_API_KEY="sk-or-v1-..." \
           -e SPRING_PROFILES_ACTIVE="home" \
           my-ayntracore:latest
```

### **Umgebung B: Kubernetes Secrets**

```yaml
# k8s-secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: ayntracore-secrets
type: Opaque
stringData:
  DATABASE_URL: postgresql://neondb_owner:npg_...@neon.tech:5432/neondb?sslmode=require
  OPENROUTER_API_KEY: sk-or-v1-ab12cd...
  SPRING_PROFILES_ACTIVE: home
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ayntracore
spec:
  template:
    spec:
      containers:
      - name: ayntracore
        image: my-ayntracore:3.0
        envFrom:
        - secretRef:
            name: ayntracore-secrets
```

Deploy:
```bash
kubectl apply -f k8s-secrets.yaml
```

### **Umgebung C: GitHub Actions (CI/CD)**

Secrets in **GitHub Repository Settings** hinterlegen:

```
Settings → Secrets and variables → Actions → New repository secret
```

Secrets hinzufügen:
- `DATABASE_URL`: `postgresql://...`
- `OPENROUTER_API_KEY`: `sk-or-v1-...`

GitHub Actions Workflow nutzt die Secrets:

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker Image
        run: docker build -t ayntracore:${{ github.sha }} .
      
      - name: Push to Registry
        run: docker push ayntracore:${{ github.sha }}
      
      - name: Deploy to Kubernetes
        env:
          DATABASE_URL: ${{ secrets.DATABASE_URL }}
          OPENROUTER_API_KEY: ${{ secrets.OPENROUTER_API_KEY }}
        run: |
          kubectl set env deployment/ayntracore \
            DATABASE_URL="${{ secrets.DATABASE_URL }}" \
            OPENROUTER_API_KEY="${{ secrets.OPENROUTER_API_KEY }}"
```

---

## 🐘 Neon PostgreSQL Setup (Schritt-für-Schritt)

### 1. Neon Account erstellen

- Gehe zu https://neon.tech
- Registriere dich (kostenlos!)
- Verifikations-Email bestätigen

### 2. Projekt & Datenbank erstellen

1. **Dashboard:** "New Project"
2. Name: `ayntracore-production`
3. Region: `EU-West-1` (Europa)
4. PostgreSQL Version: Latest (15+)
5. Click "Create"

### 3. Connection Details kopieren

Nach der Erstellung siehst du die Connection Strings:

```
Connection string (Pooler):
postgresql://neondb_owner:npg_4LDepmQGJbT7@ep-raspy-bird-...t:5432/neondb?sslmode=require

Unpooled connection:
postgresql://neondb_owner:npg_4LDepmQGJbT7@ep-raspy-bird-...eqkj.cloud.neon.tech:5432/neondb?sslmode=require
```

**Nutze IMMER den Pooler-Connection** (mit `-pooler` im Hostname)!

### 4. pgvector Extension aktivieren

```sql
-- Im Neon Query Editor:
CREATE EXTENSION IF NOT EXISTS vector;

-- Überprüfen:
SELECT * FROM pg_extension WHERE extname = 'vector';
```

### 5. Initiale Schema laden (optional)

```bash
# Falls du ein schema.sql hast:
psql "$DATABASE_URL" -f src/main/resources/db/schema.sql
```

---

## 🤖 OpenRouter API Setup

### 1. Account erstellen

- Gehe zu https://openrouter.ai
- Sign Up mit Email oder OAuth
- Verify Email

### 2. Billing Setup

1. Dashboard → Settings → Billing
2. Hinterlege Zahlungsmethode (Kreditkarte)
3. Wähle "Usage-based" Pricing
4. Budget-Limit setzen (z.B. $10/Monat für Tests)

### 3. API Key generieren

1. Settings → Keys
2. "Create Key"
3. Name: `AyntraCore-Dev` / `AyntraCore-Prod`
4. Copy den Key
5. In `.env` oder GitHub Secrets eintragen

### 4. API testen (optional)

```bash
# Test-Request:
curl "https://openrouter.ai/api/v1/chat/completions" \
  -H "Authorization: Bearer $OPENROUTER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek/deepseek-chat",
    "messages": [{"role": "user", "content": "Hello!"}]
  }'
```

---

## 🔒 Security Best Practices

### ✅ DO (Richtig)

```bash
# 1. .env im .gitignore
echo ".env" >> .gitignore

# 2. Nur placeholders in .env.example
cat .env.example  # Keine echten Werte!

# 3. Environment Variables für Production
export DATABASE_URL="postgresql://..."
export OPENROUTER_API_KEY="sk-or-v1-..."

# 4. Secrets rotieren
# Regelmäßig (alle 90 Tage):
# - Neon: Generate new credentials
# - OpenRouter: Create new key & delete old one

# 5. Zugriff beschränken
chmod 600 .env  # Nur Owner kann lesen
```

### ❌ DON'T (Falsch)

```bash
# ❌ Hardcoded in Source Code
private static final String API_KEY = "sk-or-v1-...";

# ❌ In application.properties
spring.datasource.password=npg_4LDepmQGJbT7

# ❌ In Git committen
git add .env && git commit -m "Add credentials"

# ❌ In Logs ausgeben
logger.info("Connecting to: " + connectionString);  // Shows password!

# ❌ In Fehlermeldungen sichtbar
Exception: Failed to connect: DatabaseURL=postgresql://user:PASSWORD@...
```

---

## 🛡️ Credentials Rotation & Audit

### Neon: Credentials rotieren

1. Neon Dashboard → Settings → Credentials
2. "Generate new credentials"
3. Update `.env` oder GitHub Secrets
4. Redeploy der Anwendung
5. Alte Credentials löschen

### OpenRouter: API Key rotieren

1. Dashboard → Settings → Keys
2. "Copy" neuen Key
3. Update GitHub Secrets
4. Delete alten Key
5. Redeploy

---

## 📋 Checkliste: First-Time Setup

- [ ] Neon Account erstellt & DB initialisiert
- [ ] ConnectionString (Pooler) kopiert
- [ ] OpenRouter Account & API Key generiert
- [ ] `.env` aus `.env.example` erstellt
- [ ] Credentials in `.env` eingetragen
- [ ] `.env` ist in `.gitignore` (überprüfen!)
- [ ] `mvn clean compile` erfolgreich
- [ ] `mvn spring-boot:run` mit `--spring.profiles.active=home` startet
- [ ] H2 Console nicht aktiviert für Production
- [ ] Backup der Credentials an sicherem Ort

---

## 🆘 Häufige Probleme

| Problem | Lösung |
| --- | --- |
| "Failed to connect to database" | Überprüfe CONNECTION_STRING, Firewall, Neon Status |
| "SCRAM-sha-256 auth fail" | Nutz Pooler-Host, nicht den Direct-Connection Host |
| "OpenRouter API Key invalid" | Überprüfe Key-Format (`sk-or-v1-...`) und aktualisiere .env |
| ".env wird nicht geladen" | Überprüfe Spring Boot Version hat spring-dotenv dependency |
| "Credentials in logs sichtbar" | Nutze `****` Masking oder `@Value` `@Sensitive` Annotationen |

---

**Autor:** Christian Langner  
**Version:** 3.0  
**Standard:** Enterprise Production-Ready Security  
**Letzte Aktualisierung:** Februar 2026
