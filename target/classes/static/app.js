
// Demo‑Tenant
const currentCompanyId = "550e8400-e29b-41d4-a716-446655440000";

const PERSONAS = {
  ASTRA: {
    key: "ASTRA",
    name: "Astra Noir",
    style: "Sardonisch, trocken, desinteressiert.",
    traits: { themeColor: "#a855f7", avatarUrl: "url('https://images.unsplash.com/photo-1548142813-c348350df52b?w=512&q=80')" },
    uiHints: { theming: { primaryColor: "#a855f7" }, streaming: { enabled: true }, transparency: { showSourceCategories: true } },
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
async function openSource(id) {
    const modalTitle = document.querySelector("#modalTitle");
    const modalBody = document.querySelector("#modalBody");
    const modal = document.querySelector("#modal");

    modalTitle.textContent = `Lade Quelle: ${id}...`;
    modalBody.textContent = '...';
    modal.classList.remove("hidden");

    try {
        const url = `/api/doc_fetch?companyId=${currentCompanyId}&id=${id}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        
        modalTitle.textContent = `Quelle: ${data.id} (${data.category})`;
        modalBody.textContent = data.content;

    } catch (error) {
        modalTitle.textContent = `Fehler`;
        modalBody.textContent = `Konnte die Quelle ${id} nicht laden.\n\n${error}`;
        toast(`Fehler beim Laden der Quelle: ${id}`, "warn");
    }
}
window.closeModal = () => document.querySelector("#modal").classList.add("hidden");


// RPG Media‑Galerie
window.refreshRpgGallery = async () => {
    rpgGrid.innerHTML = '<div class="text-xs text-slate-400 col-span-full">Lade Galerie...</div>';

    try {
        const response = await fetch('/api/gallery/rpg');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const items = await response.json();
        
        rpgGrid.innerHTML = ""; // Clear loading message

        if(items.length === 0){
            rpgGrid.innerHTML = '<div class="text-xs text-slate-400 col-span-full">Keine Bilder gefunden.</div>';
            return;
        }

        items.forEach(item => {
            const card = document.createElement("article");
            card.className = "relative overflow-hidden rounded-xl border border-white/10 bg-slate-900/60";
            card.innerHTML = `
                <img src="${item.url}" alt="${item.prompt}" class="w-full h-full object-cover"/>
                <div class="absolute inset-x-0 bottom-0 p-2 bg-gradient-to-t from-black/60 to-transparent text-[11px]">${item.prompt}</div>`;
            rpgGrid.appendChild(card);
        });

    } catch (error) {
        rpgGrid.innerHTML = `<div class="text-xs text-amber-400 col-span-full">Fehler beim Laden der Galerie: ${error.message}</div>`;
        toast("Fehler beim Laden der RPG-Galerie", "warn");
    }
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

const sendBtn = document.getElementById('sendBtn');
const chatInput = document.getElementById('chatInput');

sendBtn.addEventListener('click', sendMessage);
chatInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});

async function sendMessage() {
    const message = chatInput.value.trim();
    if (!message) return;

    appendMessage('user', message);
    chatInput.value = '';

    try {
        const response = await fetch('/api/agent/entry', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                companyId: currentCompanyId,
                message: message,
                mode: 'text'
            })
        });

        if (!response.ok) {
            throw new Error(`API-Fehler: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        appendMessage('agent', data.shortAnswer);
        renderRag(data.sources, 0.7);

    } catch (error) {
        console.error("Fehler beim Senden der Nachricht:", error);
        toast(error.message, 'warn');
    }
}

function appendMessage(sender, text) {
    const messageElement = document.createElement('div');
    messageElement.className = `flex items-start gap-3 ${sender === 'user' ? 'justify-end' : ''}`;

    if (sender === 'agent') {
        messageElement.innerHTML = `
            <div class="avatar h-9 w-9 rounded-full ring-2 ring-[var(--primary-color)]"></div>
            <div>
                <div class="text-xs text-slate-400">${personaName.textContent}</div>
                <div class="text-sm">${text}</div>
            </div>`;
    } else {
        messageElement.innerHTML = `
            <div>
                 <div class="text-xs text-slate-400 text-right">User</div>
                <div class="text-sm">${text}</div>
            </div>
            <div class="h-9 w-9 rounded-full bg-slate-700"></div>`;
    }

    chatStream.appendChild(messageElement);
    chatStream.scrollTop = chatStream.scrollHeight;
}

window.addEventListener("DOMContentLoaded", () => { selectApp("ASTRA"); });
