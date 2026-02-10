
# Frontend – Pose Studio (Tailwind + Bento)

## 1) Kachel (index.html Auszug)
```html
<section id="poseStudio" class="glass p-4">
  <div class="flex items-center justify-between mb-2">
    <h2 class="text-base font-semibold">Pose Studio</h2>
    <button class="text-xs underline" onclick="syncReferenceFace()">Refresh</button>
  </div>

  <div class="grid gap-3 md:grid-cols-2">
    <!-- Face Upload / Auswahl -->
    <div class="space-y-2">
      <label class="text-xs text-slate-400">Referenzgesicht</label>
      <div class="flex items-center gap-2">
        <input id="faceFile" type="file" accept="image/png,image/jpeg"
               class="text-xs" />
        <button class="px-2 py-1 rounded-lg border border-white/10 text-xs" onclick="uploadFace()">Upload</button>
      </div>
      <img id="facePreview" src="" alt="preview" class="mt-2 h-24 w-24 rounded-lg object-cover border border-white/10" />
    </div>

    <!-- Pose-Presets & Denoise -->
    <div class="space-y-2">
      <label class="text-xs text-slate-400">Pose Presets</label>
      <div class="flex flex-wrap gap-2">
        <button class="px-2 py-1 rounded-lg bg-white/10 text-xs" onclick="setPose('standing_action')">Standing Action</button>
        <button class="px-2 py-1 rounded-lg bg-white/10 text-xs" onclick="setPose('seated_profile')">Seated Profile</button>
        <button class="px-2 py-1 rounded-lg bg-white/10 text-xs" onclick="setPose('running')">Running</button>
        <button class="px-2 py-1 rounded-lg bg-white/10 text-xs" onclick="setPose('closeup')">Close-up</button>
      </div>
      <label class="text-xs text-slate-400">Denoising Strength</label>
      <input id="denoise" type="range" min="0" max="1" step="0.01" value="0.35" class="w-full accent-[var(--primary-color)]" />
      <div class="text-xs text-slate-400">Weniger = mehr Original (Pose), Mehr = mehr KI-Freiheit</div>
    </div>
  </div>
</section>
```

## 2) JS-Hooks (app.js Auszug)
```js
let posePreset = 'standing_action';

async function uploadFace(){
  const f = document.getElementById('faceFile').files[0];
  if(!f){ return toast('Kein Bild gewählt','warn'); }
  const fd = new FormData();
  fd.append('face', f);
  const personaId = window.currentPersonaId || 'demo';
  const companyId = window.currentCompanyId || 'demo-company-uuid';
  const res = await fetch(`/api/persona/${personaId}/face/upload?companyId=${companyId}`, {
    method:'POST', body: fd
  });
  if(!res.ok){ return toast('Upload fehlgeschlagen','warn'); }
  const data = await res.json();
  document.getElementById('facePreview').src = data.url;
  await fetch(`/api/persona/${personaId}/traits?companyId=${companyId}`,{
    method:'PATCH', headers:{'Content-Type':'application/json'},
    body: JSON.stringify({ image: { referenceImageUrl: data.url }})
  });
  toast('Referenz aktualisiert');
}

function setPose(p){ posePreset = p; toast(`Pose: ${p}`); }

async function generateFaceLockedImage(){
  const personaId = window.currentPersonaId || 'demo';
  const companyId = window.currentCompanyId || 'demo-company-uuid';
  const prompt = document.getElementById('chatInput').value || 'standing in a forest, action pose';
  const denoise = Number(document.getElementById('denoise').value);

  const body = { prompt, posePreset, denoiseStrength: denoise };
  const res = await fetch(`/api/image/generate?companyId=${companyId}`,{
    method:'POST', headers:{'Content-Type':'application/json'},
    body: JSON.stringify(body)
  });
  const data = await res.json();
  // TODO: Ergebnis anzeigen (Galerie/Preview)
}
```

## 3) UI-Verhalten
- Bei Upload wird die **Preview** aktualisiert und `referenceImageUrl` in den Traits gespeichert.
- Presets injizieren Keywords in den Prompt (serverseitig kann `posePreset` zusätzlich ausgewertet werden).
- Denoising-Steuerung sendet `denoiseStrength`.
