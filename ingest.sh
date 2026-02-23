#!/bin/bash

# Konfiguration
HOST="http://localhost:8080"
COMPANY_ID="550e8400-e29b-41d4-a716-446655440000"

# Funktion zum Ingestieren einer Datei
ingest_file() {
  FILE_PATH=$1
  SOURCE_ID=$(basename "$1")

  if [ ! -f "$FILE_PATH" ]; then
    echo "FEHLER: Datei nicht gefunden: $FILE_PATH"
    return 1
  fi

  # Dateiinhalt lesen und für JSON escapen
  CONTENT=$(awk '{printf "%s\\n", $0}' "$FILE_PATH" | sed 's/"/\\"/g')

  # JSON-Payload erstellen
  JSON_PAYLOAD=$(printf '{
    "companyId": "%s",
    "sourceId": "%s",
    "content": "%s"
  }' "$COMPANY_ID" "$SOURCE_ID" "$CONTENT")

  # API-Aufruf mit curl
  echo "Ingestiere $SOURCE_ID für Company $COMPANY_ID..."
  curl -X POST "$HOST/api/kb/ingest" \
       -H "Content-Type: application/json" \
       -d "$JSON_PAYLOAD"
  echo "\nIngestion für $SOURCE_ID abgeschlossen."
}

# Wissensdatenbank-Dateien ingestieren
ingest_file "Roadmap.md"
ingest_file "MISSION_STATUS.md"

echo "\nAlle Wissensquellen wurden erfolgreich ingestiert."
