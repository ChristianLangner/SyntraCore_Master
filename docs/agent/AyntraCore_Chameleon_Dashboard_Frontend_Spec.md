
# AyntraCore | Chameleon Dashboard — Frontend Spec (MD für Agenten)

**Version:** 1.0  
**Datum:** 10.02.2026  
**Zielgruppe:** Frontend‑Entwickler, Prompt/Agent‑Owner, QA

> Dieses Markdown bündelt **alle Vorgaben** für deinen anderen Agenten: Style‑Guide, Layout, App‑Szenario‑Logik, Komponenten, Security/MT‑Regeln sowie lauffähige Beispiel‑Snippets (HTML/JS mit Tailwind). Es ist als _Source‑Spec_ gedacht, nicht als endgültige Implementierung.

---

## 1) Design System & Style‑Guide (Tailwind + CSS Variablen)

### 1.1 Tokens (CSS Custom Properties)
```css
:root {
  --primary-color: #3B82F6;                /* wird per Persona überschrieben */
  --avatar-url: url('/assets/avatar.png');  /* Persona Avatar als CSS-URL */
  --glass-bg: 255 255 255 / 0.08;          /* RGBA-Anteil für Glasmorphismus */
  --glass-border: 255 255 255 / 0.18;
}
```
**Tailwind-Verwendung (Arbitrary Values):**
- Farben: `bg-[var(--primary-color)]`, `text-[var(--primary-color)]`
- Avatar: `.avatar { background-image: var(--avatar-url); }`

### 1.2 Glasmorphismus
```css
.glass {
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.18);
  border-radius: 1rem;
}
```
**Empfohlene Utilities:** `bg-white/10 backdrop-blur-md border border-white/20 shadow-xl rounded-2xl`

### 1.3 Bento Grid (Kachel‑Layouts)
- Container: `grid gap-4 auto-rows-[minmax(120px,_auto)]`
- Spalten: `grid-cols-1 md:grid-cols-2 xl:grid-cols-4`
- Große Kacheln: `md:col-span-2`, `row-span-2`

### 1.4 Typografie & States
- Headings: `text-2xl/7 md:text-3xl/8 font-semibold`
- Body: `text-sm/6 text-slate-300`
- Skeleton: `animate-pulse bg-white/10 rounded-lg`

---

## 2) Layout‑Architektur: 3‑Spalten Admin (index.html)
> Links **Sidebar/App‑Switcher**, Mitte **Chat + Persona Live‑Preview** (+ optionale RPG‑Galerie), Rechts **RAG‑Insights**.

**index.html** (Preview‑Variante mit Tailwind CDN; für Prod Tailwind builden)
```html
<!doctype html>
<html lang="de" class="h-full bg-slate-950">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>AyntraCore | Chameleon Admin</title>

    <!-- Tailwind CDN (nur Preview) -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
      tailwind.config = {
        theme: { extend: { colors: { primary: 'var(--primary-color)' } } }
      }
    </script>

    <style>
      :root {
        --primary-color: #3B82F6;
        --avatar-url: url('https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=512&q=80');
        --glass-bg: 255 255 255 / 0.08;
        --glass-border: 255 255 255 / 0.18;
      }
      .glass { background: rgba(255,255,255,0.08); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.18); border-radius: 1rem; }
      .avatar { background-image: var(--avatar-url); background-size: cover; background-position: center; }
      .scroll-shadows { mask-image: linear-gradient(to bottom, transparent, black 20px, black calc(100% - 20px), transparent); }
    </style>
  </head>
  <body class="h-full text-slate-200 selection:bg-primary/30">
    <div class="fixed inset-0 -z-10 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-primary/30 via-slate-900 to-slate-950"></div>

    <div class="mx-auto max-w-[1400px] p-4 md:p-6">
      <header class="mb-4 flex items-center justify-between">
        <h1 class="text-2xl md:text-3xl font-semibold tracking-tight">AyntraCore <span class="text-primary">Chameleon</span> Dashboard</h1>
        <div class="flex items-center gap-3">
          <div id="status-company" class="text-xs px-2 py-1 rounded-full glass">Tenant: <span id="companyIdLabel" class="text-primary">—</span></div>
          <div id="status-hints" class="text-xs px-2 py-1 rounded-full glass">Hints: <span id="hintsLabel">—</span></div>
        </div>
      </header>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
        <!-- Sidebar -->
        <aside class="lg:col-span-2 glass p-4">
          <h2 class="text-sm font-semibold mb-2 text-slate-300">App‑Szenario</h2>
          <div class="space-y-2">
            <button class="w-full px-3 py-2 rounded-lg bg-slate-800/60 hover:bg-slate-800 transition" onclick="selectApp('IHK')">IHK Wissensbot</button>
            <button class="w-full px-3 py-2 rounded-lg bg-slate-800/60 hover:bg-slate-800 transition" onclick="selectApp('RPG')">RPG Dungeon Master</button>
            <button class="w-full px-3 py-2 rounded-lg bg-slate-800/60 hover:bg-slate-800 transition" onclick="selectApp('Onboarding')">Onboarding Coach</button>
          </div>

          <div class="mt-6 space-y-4">
            <div>
              <label class="text-xs text-slate-400">minSimilarity</label>
              <input id="minSimilarity" type="range" min="0.5" max="0.95" value="0.7" step="0.01" class="w-full accent-[var(--primary-color)]">
              <div class="text-xs mt-1"><span id="minSimilarityLabel">0.70</span></div>
            </div>
            <div>
              <label class="text-xs text-slate-400">Model</label>
              <select id="modelSelect" class="mt-1 w-full rounded-lg bg-slate-900 border border-white/10 px-2 py-2">
                <option value="openai:gpt-4.1-mini">OpenAI · gpt-4.1‑mini</option>
                <option value="deepseek:chat">DeepSeek · chat</option>
                <option value="gemini-2.0-flash">Gemini · 2.0‑flash</option>
              </select>
            </div>
          </div>

          <div class="mt-6">
            <h3 class="text-sm font-semibold mb-2">Persona</h3>
            <div class="flex items-center gap-3">
              <div class="avatar h-12 w-12 rounded-full ring-2 ring-primary"></div>
              <div>
                <div id="personaName" class="text-sm font-medium">—</div>
                <div id="personaStyle" class="text-xs text-slate-400">—</div>
              </div>
            </div>
          </div>
        </aside>

        <!-- Mitte -->
        <main class="lg:col-span-6 space-y-4">
          <section class="glass p-4">
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-base font-semibold">Persona Live‑Preview</h2>
                <p class="text-xs text-slate-400">Theme: <span id="themeLabel" class="text-primary">—</span></p>
              </div>
              <div class="flex items-center gap-2">
                <span class="h-4 w-4 rounded-full bg-primary inline-block"></span>
                <span class="text-xs text-slate-400">primary</span>
              </div>
            </div>
          </section>

          <section class="glass p-4">
            <div id="chatStream" class="max-h-[540px] overflow-auto scroll-shadows space-y-3">
              <div class="animate-pulse space-y-3">
                <div class="h-5 w-1/3 bg-white/10 rounded"></div>
                <div class="h-20 w-full bg-white/10 rounded"></div>
                <div class="h-5 w-1/4 bg-white/10 rounded"></div>
              </div>
            </div>
            <div class="mt-3 flex items-center gap-2">
              <input id="chatInput" placeholder="Frage stellen…" class="flex-1 rounded-lg bg-slate-900/80 border border-white/10 px-3 py-2" />
              <button id="sendBtn" class="px-4 py-2 rounded-lg bg-primary text-white/90 hover:opacity-90">Senden</button>
            </div>
          </section>

          <section id="rpgGallery" class="hidden glass p-4">
            <div class="flex items-center justify-between mb-2">
              <h2 class="text-base font-semibold">RPG Media‑Galerie</h2>
              <button class="text-xs underline" onclick="refreshRpgGallery()">Refresh</button>
            </div>
            <div id="rpgGrid" class="grid gap-3 grid-cols-2 md:grid-cols-3 xl:grid-cols-4 auto-rows-[160px]"></div>
          </section>
        </main>

        <!-- RAG Insights -->
        <aside class="lg:col-span-4 glass p-4">
          <div class="flex items-center justify-between">
            <h2 class="text-base font-semibold">RAG‑Insights</h2>
            <button class="text-xs underline" onclick="simulateRagUpdate()">Reload</button>
          </div>
          <div id="ragSummary" class="mt-3 text-xs text-slate-400">Threshold: <span id="thresholdLabel" class="text-slate-200">0.70</span> · Model: <span id="modelLabel">—</span></div>
          <div id="ragMatches" class="mt-3 space-y-3"></div>
        </aside>
      </div>
    </div>

    <!-- Modal -->
    <div id="modal" class="fixed inset-0 z-50 hidden items-center justify-center bg-black/60">
      <div class="glass w-[min(90vw,900px)] p-4">
        <div class="flex items-center justify-between">
          <h3 id="modalTitle" class="text-base font-semibold">Quelle</h3>
          <button class="text-sm hover:text-primary" onclick="closeModal()">✕</button>
        </div>
        <div id="modalBody" class="mt-3 max-h-[60vh] overflow-auto text-sm text-slate-300 whitespace-pre-wrap"></div>
      </div>
    </div>

    <script src="./app.js"></script>
  </body>
</html>
```

---

## 3) App‑Szenario Logik (applyPersonaBranding)
> Persona‑Wechsel überschreibt CSS‑Variablen, aktiviert Features (z. B. RPG‑Galerie) und validiert **UI‑Hints** (Theming/Streaming/Transparenz) + **companyId**.

**app.js**
```js
// Demo‑Tenant
const currentCompanyId = "c6f0b7a9-6b2a-4d73-9c3c-6a2a1a6b7777";

const PERSONAS = {
  IHK: {
    key: "IHK",
    name: "IHK Wissensbot",
    style: "Professioneller Support-Stil",
    traits: { themeColor: "#0EA5E9", avatarUrl: "url('https://images.unsplash.com/photo-1548142813-c348350df52b?w=512&q=80')" },
    uiHints: { theming: { primaryColor: "#0EA5E9" }, streaming: { enabled: true }, transparency: { showSourceCategories: true } },
    features: { sourceExplorer: true, gallery: false },
    companyId: currentCompanyId
  },
  RPG: {
    key: "RPG",
    name: "Dungeon Master",
    style: "Emotionaler, immersiver Erzählstil",
    traits: { themeColor: "#A78BFA", avatarUrl: "url('https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=512&q=80')" },
    uiHints: { theming: { primaryColor: "#A78BFA" }, streaming: { enabled: true }, transparency: { showSourceCategories: true } },
    features: { sourceExplorer: false, gallery: true },
    companyId: currentCompanyId
  },
  Onboarding: {
    key: "Onboarding",
    name: "Onboarding Coach",
    style: "Empathisch, klar strukturiert",
    traits: { themeColor: "#22C55E", avatarUrl: "url('https://images.unsplash.com/photo-1502685104226-ee32379fefbe?w=512&q=80')" },
    uiHints: { theming: { primaryColor: "#22C55E" }, streaming: { enabled: true }, transparency: { showSourceCategories: true } },
    features: { sourceExplorer: false, gallery: false },
    companyId: currentCompanyId
  }
};

const $ = (q) => document.querySelector(q);
const chatStream = document.querySelector("#chatStream");
const personaName = document.querySelector("#personaName");
const personaStyle = document.querySelector("#personaStyle");
const themeLabel = document.querySelector("#themeLabel");
const companyIdLabel = document.querySelector("#companyIdLabel");
const hintsLabel = document.querySelector("#hintsLabel");
const thresholdLabel = document.querySelector("#thresholdLabel");
const modelLabel = document.querySelector("#modelLabel");
const minSimilarityInput = document.querySelector("#minSimilarity");
const minSimilarityLabel = document.querySelector("#minSimilarityLabel");
const modelSelect = document.querySelector("#modelSelect");
const rpgGallery = document.querySelector("#rpgGallery");
const rpgGrid = document.querySelector("#rpgGrid");

function applyPersonaBranding(persona) {
  // 1) CSS Variablen setzen
  document.documentElement.style.setProperty("--primary-color", persona.traits.themeColor);
  document.documentElement.style.setProperty("--avatar-url", persona.traits.avatarUrl);

  // 2) Labels
  personaName.textContent = persona.name;
  personaStyle.textContent = persona.style;
  themeLabel.textContent = persona.traits.themeColor;
  companyIdLabel.textContent = persona.companyId || "—";

  // 3) Feature Toggles
  rpgGallery.classList.toggle("hidden", !persona.features.gallery);

  // 4) UI‑Hints validieren
  const { ok } = validateUiHints(persona);
  hintsLabel.textContent = ok ? "OK" : "WARN";
  hintsLabel.className = ok ? "text-green-400" : "text-amber-400";

  // 5) Security Quick Check
  if (!persona.companyId) toast("Fehlende companyId – Multi-Tenancy Guard aktiv!", "warn");

  // 6) Chat reset
  resetChatSkeleton();
}

function validateUiHints(persona) {
  const h = persona.uiHints || {};
  const themingOk = !!(h.theming && h.theming.primaryColor);
  const streamingOk = !!(h.streaming && h.streaming.enabled === true);
  const transparencyOk = !!(h.transparency && h.transparency.showSourceCategories === true);
  const companyOk = !!persona.companyId;
  return { ok: themingOk && streamingOk && transparencyOk && companyOk };
}

window.selectApp = (key) => {
  const persona = PERSONAS[key];
  if (!persona) return;
  applyPersonaBranding(persona);
  modelLabel.textContent = modelSelect.value;
  thresholdLabel.textContent = toScore(minSimilarityInput.value);
  simulateRagUpdate();
  if (persona.features.gallery) refreshRpgGallery();
};

// RAG‑Insights Demo
window.simulateRagUpdate = () => {
  const minScore = Number(minSimilarityInput.value);
  const matches = [
    { id: "kb_421", category: "Policies", score: 0.81, snippet: "…data retention rules…" },
    { id: "kb_122", category: "API",      score: 0.73, snippet: "…v1 migration steps…" },
    { id: "kb_222", category: "Runbook",  score: 0.66, snippet: "…restart vector index…" }
  ];
  renderRag(matches, minScore);
};

function renderRag(matches, minScore) {
  const container = document.querySelector("#ragMatches");
  container.innerHTML = "";
  matches.forEach(m => {
    const warn = m.score < minScore;
    const card = document.createElement("div");
    card.className = "rounded-lg bg-slate-900/70 border border-white/10 p-3";
    card.innerHTML = `
      <div class="flex items-center justify-between mb-1">
        <div class="text-sm font-medium">${m.id} · ${m.category}</div>
        <div class="text-xs ${warn ? 'text-amber-400' : 'text-slate-400'}">${m.score.toFixed(2)}</div>
      </div>
      <div class="h-2 w-full bg-slate-800 rounded"><div class="h-2 ${warn ? 'bg-amber-400' : 'bg-[var(--primary-color)]'} rounded" style="width:${(m.score*100).toFixed(0)}%"></div></div>
      <div class="mt-1 text-[11px] text-slate-400">${warn ? '[LOW_RELEVANCE_WARNING]' : '[VECTOR_MATCH]'} ${m.id}</div>
      <div class="mt-2 flex items-center gap-2">
        <button class="text-xs underline" onclick="openSource('${m.id}')">Volltext anzeigen</button>
        <span class="text-[11px] text-slate-500">${m.category}</span>
      </div>`;
    container.appendChild(card);
  });
}

// Source Explorer (IHK)
window.openSource = (id) => {
  document.querySelector("#modalTitle").textContent = `Quelle: ${id}`;
  document.querySelector("#modalBody").textContent = `Volltext (Demo) für ${id}

Hier würde der vollständige KB-Text erscheinen…`;
  document.querySelector("#modal").classList.remove("hidden");
};
window.closeModal = () => document.querySelector("#modal").classList.add("hidden");

// RPG Media‑Galerie
window.refreshRpgGallery = () => {
  rpgGrid.innerHTML = "";
  const demo = [
    { id: "civ_1001", url: "https://images.unsplash.com/photo-1604079628040-94301bb21b91?w=512&q=80", prompt: "Ancient dungeon gate" },
    { id: "civ_1002", url: "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=512&q=80", prompt: "Misty valley" },
    { id: "civ_1003", url: "https://images.unsplash.com/photo-1549880338-65ddcdfd017b?w=512&q=80", prompt: "Crystal cavern" },
    { id: "civ_1004", url: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=512&q=80", prompt: "Arcane library" }
  ];
  demo.forEach(item => {
    const card = document.createElement("article");
    card.className = "relative overflow-hidden rounded-xl border border-white/10 bg-slate-900/60";
    card.innerHTML = `
      <img src="${item.url}" alt="${item.prompt}" class="w-full h-full object-cover"/>
      <div class="absolute inset-x-0 bottom-0 p-2 bg-gradient-to-t from-black/60 to-transparent text-[11px]">${item.prompt}</div>`;
    rpgGrid.appendChild(card);
  });
};

// Chat / Toast / Bindings
function resetChatSkeleton() {
  chatStream.innerHTML = `
    <div class="flex items-start gap-3">
      <div class="avatar h-9 w-9 rounded-full ring-2 ring-[var(--primary-color)]"></div>
      <div>
        <div class="text-xs text-slate-400">System</div>
        <div class="text-sm">Theming aktiv: <span class="text-[var(--primary-color)]">${getComputedStyle(document.documentElement).getPropertyValue('--primary-color').trim()}</span></div>
        <div class="mt-1 text-xs text-slate-400">Streaming: enabled · Sources: categories visible</div>
      </div>
    </div>`;
}

const toScore = (v) => Number(v).toFixed(2);
minSimilarityInput.addEventListener("input", () => {
  minSimilarityLabel.textContent = toScore(minSimilarityInput.value);
  thresholdLabel.textContent = toScore(minSimilarityInput.value);
});
modelSelect.addEventListener("change", () => { modelLabel.textContent = modelSelect.value; });

function toast(msg, type = "info") {
  const el = document.createElement("div");
  el.className = `fixed bottom-5 left-1/2 -translate-x-1/2 px-3 py-2 rounded-lg glass text-sm ${type === 'warn' ? 'text-amber-300' : 'text-slate-200'}`;
  el.textContent = msg;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 2400);
}

window.addEventListener("DOMContentLoaded", () => { selectApp("IHK"); });
```

---

## 4) Komponenten je Szenario
- **RPG**: `#rpgGallery` (Bento‑Grid) rendert generierte Bilder (Platzhalter → später via API ersetzen).  
  Hook: `refreshRpgGallery()`
- **IHK**: **Source Explorer** (Modal) mit `openSource(id)` → lädt/zeigt Volltext der Quelle.  
  Ersetze Demo durch `fetch('/api/doc_fetch?companyId=…&id=…')`.

---

## 5) Security, RAG & Observability (Frontend‑Belange)
- **Multi‑Tenancy**: Anzeige des aktuellen Tenants in der Kopfzeile; Fehlerzustand, wenn `companyId` fehlt.
- **RAG‑Panel**: `renderRag(matches, minScore)` zeigt Score‑Balken, `[VECTOR_MATCH]` / `[LOW_RELEVANCE_WARNING]` Labels & Kategorien.
- **Konfig**: `minSimilarity` und `model` sind UI‑Parameter, die 1:1 an Backend/Agent übergeben werden.

---

## 6) Definition of Done (DoD)
- Persona‑Wechsel ändert **Farbe** + **Avatar** (CSS‑Variablen) _ohne Reload_.
- RAG‑Insights visualisieren mind. **id**, **category**, **score** inkl. Threshold‑Warnung.
- IHK‑Modal zeigt Volltext (echter API‑Call integriert).
- RPG‑Galerie lädt/zeigt Medienkarten (ECHTE API oder CDN‑Assets).
- UI‑Hints geprüft: `theming.primaryColor`, `streaming.enabled`, `transparency.showSourceCategories`, `companyId`.

---

## 7) Build‑Hinweise (Prod)
- Verwende Tailwind CLI oder PostCSS Build; purge content (`./index.html`, `./app.js`, ggf. Template‑Dateien).
- CDN nur für lokale Demos. In Prod: Fingerprinting/Minification aktivieren.

---

**Ende der Spec**
