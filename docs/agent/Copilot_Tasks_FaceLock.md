
# Copilot Tasks – Image-to-Image with Face-Lock Integration

## 1) Backend
- Erweitere `ImageGenerationParams` um `referenceImageUrl`, `denoiseStrength`, `posePreset`.
- Implementiere `FaceImagePort` und `CivitaiReplicateImageAdapter.generateWithFace(ImageCallArgs)`.
- Lade `referenceImageUrl` aus `persona.traits.image.referenceImageUrl` wenn nicht via UI gesetzt.
- Validierung: `referenceImageUrl` muss `/tenants/{companyId}/` enthalten.

## 2) Frontend
- Baue die **Pose Studio** Kachel: Upload, Preview, Presets, Denoising-Slider.
- `uploadFace()` -> POST Upload; `PATCH` Traits.
- `generateFaceLockedImage()` -> POST `/api/image/generate` mit `posePreset` + `denoiseStrength`.

## 3) Sicherheit
- Enforce `companyId` für alle Routen.
- Upload-Validierung: MIME, Größe. Pfad includes `/tenants/{companyId}/`.

## 4) Tests (DoD)
- 3 Läufe mit Face-Lock: gleiches Gesicht, veränderte Pose per Preset.
- Negative: fehlende `companyId` -> 403; Fremdpfad -> 403; falsches MIME -> 415; >5MB -> 413.
- Logs: `[IMAGE_CALL]` enthält maskierte `referenceImageUrl` + `[LATENCY]`.
