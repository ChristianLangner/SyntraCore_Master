
# AyntraCore | Image Generation Bridge & UI-Integration (Frontend/Backend Spec)

**Version:** 1.0  
**Datum:** 10.02.2026  
**Bezug:** `ayntracore_blueprint.md`, `6_Java_Snippets.md`

Dieses Dokument definiert die **Datenstruktur (Ebene 2)** für Bildgenerierung, die **Admin‑UI Sidebar** (Ebene 3) und die **Laufzeitlogik**. Am Ende findest du einen **Ablaufplan für den Agenten**.

---

## 1) Backend – Datenstruktur (Ebene 2)

### 1.1 `ImageGenerationMetadata` (persistiert in Persona‑Traits / JSONB)
> Single Source of Truth der Bild‑Defaultwerte je Persona/Tenant.

**Java 21 Record**
```java
// Core: reines Datenmodell (keine Framework-Annotationen)
public record ImageGenerationMetadata(
    Long seed,
    String modelId,
    String sampler,
    int steps,
    double cfgScale
) {}
```

**Beispiel (traits JSONB)**
```json
{
  "themeColor": "#0EA5E9",
  "image": {
    "seed": 123456789,
    "modelId": "civitai/realvisxl-v4",
    "sampler": "DPM++ 2M Karras",
    "steps": 28,
    "cfgScale": 6.5
  }
}
```

**Persistenz (Infrastructure Adapter, JSONB – hypersistence-utils)**
```java
// In der Persistence-Ebene (Adapter) – Beispielauszug
@Entity
@Table(name = "persona")
public class PersonaEntity {
  @Id UUID id;
  String name;

  @Type(org.hibernate.type.JsonType.class) // hypersistence-utils
  @Column(columnDefinition = "jsonb")
  Map<String, Object> traits; // enthält u.a. den "image"-Block
}
```

### 1.2 `ImageGenerationParams` (Runtime‑Aufrufparameter)
> Wird pro Request erzeugt (UI‑Overrides + Defaults aus Metadata).

**Java 21 Record (gemäß Aufgabenstellung)**
```java
public record ImageGenerationParams(
    String prompt,
    Long seed,
    String model,
    Double cfgScale
) {}
```

**Kompatibilität zu Metadata**  
Optional erweiterbar um `sampler`/`steps` – Standard kommt aus `ImageGenerationMetadata`:
```java
public record ImageCallArgs(
    String prompt,
    Long seed,
    String modelId,
    String sampler,
    Integer steps,
    Double cfgScale
) {
  public static ImageCallArgs merge(ImageGenerationMetadata meta, ImageGenerationParams in) {
    return new ImageCallArgs(
      in.prompt(),
      (in.seed() != null ? in.seed() : meta.seed()),
      (in.model() != null ? in.model() : meta.modelId()),
      meta.sampler(),
      meta.steps(),
      (in.cfgScale() != null ? in.cfgScale() : meta.cfgScale())
    );
  }
}
```

### 1.3 PersonaService – Seed‑Initialisierung (COMPANION)
```java
public final class PersonaService {
  private final PersonaRepository personaRepository; // Adapter-Port

  public Persona createCompanionPersona(String name) {
    Persona p = new Persona(name, /* weitere Felder */);
    // zufälliger Seed, falls in Traits fehlt
    long seed = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    Map<String, Object> image = Map.of(
        "seed", seed,
        "modelId", "civitai/realvisxl-v4",
        "sampler", "DPM++ 2M Karras",
        "steps", 28,
        "cfgScale", 6.5
    );
    p.putTrait("image", image);
    return personaRepository.save(p);
  }
}
```

### 1.4 Adapter – CivitaiImageAdapter (Seed‑Konsistenz)
```java
public interface ImageProviderPort {
  ImageResult generate(ImageCallArgs args);
}

@RequiredArgsConstructor
public class CivitaiImageAdapter implements ImageProviderPort {
  private final HttpClient http; // Pseudocode
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CivitaiImageAdapter.class);

  @Override
  public ImageResult generate(ImageCallArgs args) {
    long t0 = System.nanoTime();
    // HTTP-Aufruf vorbereitet (Pseudocode)
    // POST /images { prompt, seed, modelId, sampler, steps, cfgScale }
    log.info("[IMAGE_CALL] model:{} seed:{} cfg:{} sampler:{} steps:{}", args.modelId(), args.seed(), args.cfgScale(), args.sampler(), args.steps());
    // ... Response parsen
    long ms = (System.nanoTime() - t0) / 1_000_000L;
    log.info("[LATENCY] image_ms:{}", ms);
    return new ImageResult(/* url, meta ... */);
  }
}
```

---

## 2) Frontend – "Cool & Uncomplicated" Sidebar (Ebene 3)

### 2.1 HTML (Slide‑Over Sidebar in `admin.html`)
```html
<!-- Trigger: Bild/Preview klickbar -->
<img src="/assets/preview.jpg" alt="preview" class="cursor-pointer rounded-xl" onclick="openImageSidebar()"/>

<!-- Slide-over Sidebar -->
<aside id="imgSidebar" class="fixed inset-y-0 right-0 z-50 hidden w-[380px] glass p-4 border-l border-white/10">
  <div class="flex items-center justify-between mb-3">
    <h3 class="text-base font-semibold">Bild‑Einstellungen</h3>
    <button class="text-sm hover:text-[var(--primary-color)]" onclick="closeImageSidebar()">✕</button>
  </div>

  <label class="text-xs text-slate-400">Prompt</label>
  <textarea id="imgPrompt" class="mt-1 w-full h-28 rounded-lg bg-slate-900/70 border border-white/10 p-2 text-sm"></textarea>

  <div class="mt-3 grid grid-cols-2 gap-3">
    <div>
      <label class="text-xs text-slate-400">Seed</label>
      <div class="mt-1 flex items-center gap-2">
        <input id="imgSeed" type="number" class="flex-1 rounded-lg bg-slate-900/70 border border-white/10 px-2 py-1 text-sm"/>
        <button id="seedLockBtn" class="px-2 py-1 rounded-lg border border-white/10 hover:bg-white/5 text-xs" title="Seed lock">🔒</button>
      </div>
    </div>
    <div>
      <label class="text-xs text-slate-400">CFG</label>
      <input id="imgCfg" type="number" step="0.1" min="1" max="15" class="mt-1 w-full rounded-lg bg-slate-900/70 border border-white/10 px-2 py-1 text-sm"/>
    </div>
  </div>

  <div class="mt-3">
    <label class="text-xs text-slate-400">Model</label>
    <input id="imgModel" class="mt-1 w-full rounded-lg bg-slate-900/70 border border-white/10 px-2 py-1 text-sm" placeholder="civitai/realvisxl-v4"/>
  </div>

  <div class="mt-4">
    <div class="text-xs text-slate-400 mb-1">Quick‑Presets</div>
    <div class="flex flex-wrap gap-2">
      <button class="px-2 py-1 rounded-lg bg-white/10 hover:bg-white/15 text-xs" onclick="applyPreset('photoreal')">Photorealistic</button>
      <button class="px-2 py-1 rounded-lg bg-white/10 hover:bg-white/15 text-xs" onclick="applyPreset('anime')">Anime</button>
      <button class="px-2 py-1 rounded-lg bg-white/10 hover:bg-white/15 text-xs" onclick="applyPreset('render3d')">3D‑Render</button>
    </div>
  </div>

  <div class="mt-5 flex items-center gap-2">
    <button class="px-3 py-2 rounded-lg bg-[var(--primary-color)] text-white/90" onclick="saveImageTraits()">Speichern</button>
    <button class="px-3 py-2 rounded-lg border border-white/10" onclick="generateImage()">Generieren</button>
  </div>
</aside>
```

### 2.2 JavaScript (Seed‑Lock + Presets + Hot‑Persist)
```html
<script>
  let seedLocked = true; // UI-Status
  const personaId = window.currentPersonaId || "demo";

  function openImageSidebar(){ document.getElementById('imgSidebar').classList.remove('hidden'); }
  function closeImageSidebar(){ document.getElementById('imgSidebar').classList.add('hidden'); }

  document.getElementById('seedLockBtn').addEventListener('click', () => {
    seedLocked = !seedLocked;
    document.getElementById('seedLockBtn').textContent = seedLocked ? '🔒' : '🔓';
  });

  function applyPreset(kind){
    const cfg = document.getElementById('imgCfg');
    const model = document.getElementById('imgModel');
    switch(kind){
      case 'photoreal': cfg.value = 6.5; model.value = 'civitai/realvisxl-v4'; break;
      case 'anime': cfg.value = 8.0; model.value = 'civitai/anything-v5'; break;
      case 'render3d': cfg.value = 7.0; model.value = 'civitai/3d-realistic-v1'; break;
    }
  }

  async function saveImageTraits(){
    const payload = {
      image: {
        seed: Number(document.getElementById('imgSeed').value) || null,
        modelId: document.getElementById('imgModel').value || null,
        cfgScale: Number(document.getElementById('imgCfg').value) || null,
        seedLock: seedLocked
      }
    };
    await fetch(`/api/persona/${personaId}/traits`, {
      method: 'PATCH', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
  }

  async function generateImage(){
    const seedField = document.getElementById('imgSeed');
    const seed = seedLocked && seedField.value ? Number(seedField.value) : Math.floor(Math.random()*1e12);

    const params = {
      prompt: document.getElementById('imgPrompt').value,
      seed: seed,
      model: document.getElementById('imgModel').value || null,
      cfgScale: Number(document.getElementById('imgCfg').value) || null
    };

    // Optional: Hot-Reload Persist (schreibe Seed zurück, falls unlocked Exploration)
    if(!seedLocked){
      await fetch(`/api/persona/${personaId}/traits`, {
        method: 'PATCH', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ image: { seed: seed } })
      });
    }

    // Trigger Server-Call (Adapter → Civitai)
    const res = await fetch(`/api/image/generate`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params)
    });
    const data = await res.json();
    // TODO: data.url in die Galerie pushen
  }
</script>
```

---

## 3) Laufzeit‑Logik (Hot‑Reload & Konsistenz)
- **Seed‑Lock** zu → Seed kommt aus `traits.image.seed` (konsistentes Gesicht/Look).  
- **Seed‑Lock** offen → pro Request neuen Seed würfeln; optional direkt in `traits.image.seed` persistieren (Exploration nachvollziehbar).  
- **Presets** setzen Model/CFG (und optional Sampler/Steps) lokal; beim Speichern werden Änderungen sofort via `PATCH /traits` persistiert (Hot‑Reload).  
- **Backpressure/Throttling**: Änderungen an Traits entprellen (300–500ms), um unnötige Writes zu vermeiden.  
- **Logging**: `[IMAGE_CALL] model:… seed:… cfg:…` + `[LATENCY] image_ms:…` im Adapter.

---

## 4) Ablaufsplan für den Agenten (Execution Flow)

### 4.1 Preflight
1. **companyId prüfen** (Multi‑Tenancy Gate).  
2. **Persona laden** → `traits.image` (`seed`, `modelId`, `sampler`, `steps`, `cfgScale`, optional `seedLock`).

### 4.2 Parameteraufbau
3. UI‑Eingaben lesen: `prompt`, `cfgScale`, `model` (+ ggf. `seedLock`).  
4. **Seed bestimmen**:  
   - Wenn `seedLock == true` → nimm `traits.image.seed`.  
   - Sonst → generiere neuen Seed; **optional** back‑persist in `traits.image.seed` (Exploration).
5. **Merge**: `ImageCallArgs = merge(metadata, params)` (Defaults aus Metadata, UI überschreibt, wo gesetzt).

### 4.3 Aufruf & Ausgabe
6. **Adapter aufrufen** → `CivitaiImageAdapter.generate(ImageCallArgs)`; Latenz loggen.  
7. **Ergebnis** (Bild‑URL) in **RPG‑Galerie**/Preview einfügen.  
8. **Audit/Traits** aktualisieren: `lastModel`, `lastSeed`, `updatedAt`.

### 4.4 UI‑Hints & Sicherheit
9. **UI‑Hints** zurückgeben: `{ theming.primaryColor, streaming.enabled, transparency.showSourceCategories }`.  
10. Fehlerfälle: fehlende `companyId` → Blockieren; ungültige `cfgScale`/`model` → Validierungsfeedback.

### 4.5 Tests (DoD)
- **Seed‑Konsistenz**: Gleiches Prompt + SeedLock=true → visuell konsistente Ergebnisse in ≥ 3 Läufen.  
- **Presets** ändern CFG/Model wie definiert.  
- **Persistenz**: PATCH auf `/traits` schreibt `image`‑Block korrekt (JSONB).  
- **Logging** vorhanden (`[IMAGE_CALL]`, `[LATENCY]`).

---

## 5) 💬 Anweisung für Copilot (Copy & Paste)

```md
# TASK: Image Generation Bridge & UI-Integration

Basierend auf 'ayntracore_blueprint.md' und '6_Java_Snippets.md', implementieren wir das Image-Handling-System.

### 1. Backend (Java 21)
- Erstelle das Record 'ImageGenerationParams(String prompt, Long seed, String model, Double cfgScale)'.
- Erweitere den 'PersonaService', sodass beim Erstellen einer Persona (Typ COMPANION) automatisch ein zufälliger Seed in den 'traits' gespeichert wird.
- Stelle sicher, dass dieser Seed bei jedem Call an den 'CivitaiImageAdapter' mitgegeben wird, um visuelle Konsistenz zu garantieren.

### 2. Frontend (Tailwind CSS)
- Erstelle eine minimalistische Sidebar-Komponente in 'admin.html' für die Bild-Einstellungen.
- Implementiere ein 'Seed-Lock' Feature: Ein Toggle, der entscheidet, ob der Seed aus den Traits festgeschrieben oder bei jedem Request neu gewürfelt wird.
- Nutze Glasmorphismus (backdrop-blur-md) für die Sidebar, um das Design modern und sauber zu halten.

### 3. Logik
- Alle Änderungen in der Sidebar müssen sofort per AJAX/WebSocket zurück in das 'traits' Feld in der DB geschrieben werden (Hot-Reload).
```

---

## 6) Nächste Schritte
- **Sidebar in `admin.html` integrieren** und mit realen Endpunkten verdrahten (`/api/persona/:id/traits`, `/api/image/generate`).  
- **Adapter gegen echten Provider testen** (Civitai/OpenRouter etc.).  
- **E2E‑Prompts** erstellen: SeedLock=true/false, Presets, Persistenzpfad.

