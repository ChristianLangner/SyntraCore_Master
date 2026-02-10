
# AyntraCore | Face-Lock & Pose Studio – Technisches Umsetzungskonzept (Agent-ready)

**Version:** 1.0  
**Datum:** 2026-02-10  
**Ziel:** Die Identität (Gesicht) wird von der Struktur (Pose/Körper) getrennt. Backend erhält einen Face-Port/Adapter; das Frontend bekommt ein Pose Studio. Multi-Tenancy bleibt strikt.

---

## 1) Architekturüberblick
- **Ebene 2 (Backend/Core+Adapter):**
  - Erweiterung `ImageGenerationParams` um `referenceImageUrl` (und optional `denoiseStrength`, `posePreset`).
  - Neuer **Face-Port** (Schnittstelle) plus **Adapter** zu Civitai/Replicate; Nutzung von Face-Referenzen (z. B. IP-Adapter/InstantID) und Control-Images.
  - Traits-Erweiterung: `persona.traits.image.referenceImageUrl` als Master-Face pro Persona/Tenant.

- **Ebene 3 (Frontend):**
  - **Pose Studio** als Kachel im Chameleon Dashboard (Bento-Grid): Upload/Select für Referenzgesicht, Pose-Presets (Keywords), Denoising-Slider.

- **Sicherheit:**
  - Alle Uploads **tenant-scoped** ablegen: `/tenants/{companyId}/faces/{personaId}/<filename>`.
  - Jede API erfordert `companyId` (403 bei Fehlen). MIME/Size-Checks.

---

## 2) Datenmodell (Traits & Params)
- `persona.traits.image.referenceImageUrl: string` (z. B. `https://cdn.example/tenants/{companyId}/faces/{personaId}/master.jpg`)
- `persona.traits.image.seed: long` (bestehend)
- `ImageGenerationParams` (erweitert): `referenceImageUrl: String`, `denoiseStrength: Double`, `posePreset: String`

---

## 3) High-Level Flow (Image-to-Image mit Face-Lock)
1. **Persona laden** -> Traits lesen (inkl. `referenceImageUrl`).
2. **UI-Eingaben** (Pose-Preset, Denoising, Prompt) übernehmen.
3. **Merge**: Defaults (Metadata) + UI in `ImageCallArgs` (mit `referenceImageUrl`).
4. **Adapter-Call**: POST zum Provider mit Feldern `{ prompt, seed, modelId, sampler, steps, cfgScale, referenceImageUrl, posePreset, denoiseStrength }`.
5. **Antwort**: Bild-URL. Audit/Traits ggf. aktualisieren (`lastSeed`, `lastModel`).

---

## 4) DoD (Definition of Done)
- Face-Lock: Gleiches Gesicht über mindestens 3 Läufe konsistent; Pose wechselt je Preset.
- Frontend: Upload, Preview, Presets, Denoising-Slider funktionieren und persistieren Settings im Trait-Feld.
- Security: Uploads strikt tenant-scoped; 403 ohne `companyId`.
- Logging: `[IMAGE_CALL]` inkl. `referenceImageUrl` (maskiert auf Host/Path, ohne PII) + `[LATENCY] image_ms`.

---

## 5) Offene Erweiterungen
- Optional `faceConfidence` (Double) zur Gewichtung des Face-Locks.
- Erweitertes Pose-Control via OpenPose/ControlNet (kann später ergänzt werden).
