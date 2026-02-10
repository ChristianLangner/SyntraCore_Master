
# Backend – Face-Port & Adapter (Java 21)

## 1) Records & DTOs

```java
// Bereits vorhanden, jetzt erweitert
public record ImageGenerationParams(
    String prompt,
    Long seed,
    String model,
    Double cfgScale,
    String referenceImageUrl,   // neu
    Double denoiseStrength,     // optional UI-Override
    String posePreset           // optional UI-Override (z. B. "standing_action")
) {}

public record ImageGenerationMetadata(
    Long seed,
    String modelId,
    String sampler,
    int steps,
    double cfgScale
) {}

public record ImageCallArgs(
    String prompt,
    Long seed,
    String modelId,
    String sampler,
    Integer steps,
    Double cfgScale,
    String referenceImageUrl,
    Double denoiseStrength,
    String posePreset
) {
  public static ImageCallArgs merge(ImageGenerationMetadata meta, ImageGenerationParams in, String traitsReferenceUrl) {
    return new ImageCallArgs(
      in.prompt(),
      (in.seed() != null ? in.seed() : meta.seed()),
      (in.model() != null ? in.model() : meta.modelId()),
      meta.sampler(),
      meta.steps(),
      (in.cfgScale() != null ? in.cfgScale() : meta.cfgScale()),
      (in.referenceImageUrl() != null ? in.referenceImageUrl() : traitsReferenceUrl),
      in.denoiseStrength(),
      in.posePreset()
    );
  }
}
```

## 2) Ports & Adapter

```java
public interface FaceImagePort {
  ImageResult generateWithFace(ImageCallArgs args);
}

@RequiredArgsConstructor
public class CivitaiReplicateImageAdapter implements FaceImagePort {
  private final HttpClient http; // Pseudocode
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CivitaiReplicateImageAdapter.class);

  @Override
  public ImageResult generateWithFace(ImageCallArgs args) {
    long t0 = System.nanoTime();

    // Sicherheit: referenceImageUrl muss tenant-scoped sein
    assertTenantScoped(args.referenceImageUrl());

    // Provider-spezifisches Payload-Mapping (Beispiel-Schlüssel)
    var payload = Map.of(
      "prompt", args.prompt(),
      "seed", args.seed(),
      "model", args.modelId(),
      "sampler", args.sampler(),
      "steps", args.steps(),
      "cfg_scale", args.cfgScale(),
      // Face-Lock Felder (je nach Provider: ip_adapter_image / instant_id_image)
      "ip_adapter_image", args.referenceImageUrl(),
      // Optional Pose & Denoise
      "pose_preset", args.posePreset(),
      "denoise_strength", args.denoiseStrength()
    );

    log.info("[IMAGE_CALL] model:{} seed:{} refFace:{} pose:{} cfg:{} steps:{}", args.modelId(), args.seed(), maskUrl(args.referenceImageUrl()), args.posePreset(), args.cfgScale(), args.steps());

    // http.post(...)
    long ms = (System.nanoTime() - t0) / 1_000_000L;
    log.info("[LATENCY] image_ms:{}", ms);
    return new ImageResult(/* url, meta */);
  }

  private void assertTenantScoped(String url) {
    if (url == null || !url.contains("/tenants/")) {
      throw new SecurityException("referenceImageUrl must be tenant-scoped");
    }
  }

  private String maskUrl(String url) {
    if (url == null) return "null";
    try {
      java.net.URI u = new java.net.URI(url);
      return u.getScheme() + "://" + u.getHost() + "/…";
    } catch (Exception e) {
      return "…";
    }
  }
}
```

## 3) PersonaService – Reference Management

```java
public class PersonaService {
  public void setReferenceFace(UUID personaId, UUID companyId, String referenceUrl) {
    // Load persona by personaId + companyId
    // Update traits.image.referenceImageUrl = referenceUrl
    // Persist JSONB
  }
}
```

## 4) Multi-Tenancy & Validierung
- Jede Operation erfordert `companyId`.
- referenceImageUrl muss Pfadsegment `/tenants/{companyId}/` enthalten.
- Upload-Whitelist: `image/jpeg`, `image/png`, max. Größe z. B. 5 MB.
