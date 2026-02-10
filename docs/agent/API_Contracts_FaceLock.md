
# API Contracts – Face-Lock & Pose Studio

## 1) Upload Referenzgesicht
```http
POST /api/persona/{personaId}/face/upload?companyId={uuid}
Content-Type: multipart/form-data

files: { face: <binary> }
```
**Antwort**
```json
{
  "url": "https://cdn.example/tenants/{companyId}/faces/{personaId}/master.jpg"
}
```
**Regeln**
- Dateiname enthält `companyId` im Pfad: `/tenants/{companyId}/faces/{personaId}/...`
- MIME: `image/jpeg` oder `image/png`, max 5 MB

## 2) Traits aktualisieren (Reference URL)
```http
PATCH /api/persona/{personaId}/traits?companyId={uuid}
Content-Type: application/json

{
  "image": { "referenceImageUrl": "https://cdn.example/tenants/{companyId}/faces/{personaId}/master.jpg" }
}
```

## 3) Image Generate (Face-Lock)
```http
POST /api/image/generate?companyId={uuid}
Content-Type: application/json

{
  "prompt": "standing in a forest, action pose",
  "seed": 123456789,
  "model": "civitai/realvisxl-v4",
  "cfgScale": 6.5,
  "referenceImageUrl": "https://cdn.example/tenants/{companyId}/faces/{personaId}/master.jpg",
  "denoiseStrength": 0.35,
  "posePreset": "standing_action"
}
```
**Antwort**
```json
{ "url": "https://cdn.example/tenants/{companyId}/images/xyz.png" }
```

## 4) Referenz abrufen
```http
GET /api/persona/{personaId}/face?companyId={uuid}
200 OK
{ "referenceImageUrl": "..." }
```

## 5) Fehlerfälle
- 400: Ungültige oder fehlende Felder
- 403: companyId fehlt oder Referenzpfad nicht tenant-scoped
- 415: Nicht unterstütztes Bildformat
- 413: Datei zu groß
