
# Test Prompts – Face-Lock & Pose Studio

## 1) Upload Referenzgesicht
```bash
curl -sS -X POST "http://localhost/api/persona/<PERSONA_ID>/face/upload?companyId=<TENANT_UUID>"   -F "face=@/path/to/master.jpg" | jq
```

## 2) Traits setzen
```bash
curl -sS -X PATCH "http://localhost/api/persona/<PERSONA_ID>/traits?companyId=<TENANT_UUID>"   -H "Content-Type: application/json"   -d '{"image":{"referenceImageUrl":"https://cdn.example/tenants/<TENANT_UUID>/faces/<PERSONA_ID>/master.jpg"}}' | jq
```

## 3) Generate (Face-Lock, Preset, Denoising)
```bash
curl -sS -X POST "http://localhost/api/image/generate?companyId=<TENANT_UUID>"   -H "Content-Type: application/json"   -d '{
    "prompt":"standing in a forest, action pose",
    "posePreset":"standing_action",
    "denoiseStrength":0.35
  }' | jq
```

## 4) Negative Tests
- Ohne `companyId` -> 403
- `referenceImageUrl` ohne `/tenants/<TENANT_UUID>/` -> 403
- Upload `text/plain` -> 415
- Upload > 5 MB -> 413
