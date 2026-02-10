
const currentCompanyId = "demo-company-uuid";
const PERSONAS = {
  IHK: { key:'IHK', name:'IHK Wissensbot', style:'Formal', traits:{ themeColor:'#0EA5E9', avatarUrl:"url('https://images.unsplash.com/photo-1548142813-c348350df52b?w=256&q=80')" }, uiHints:{ theming:{primaryColor:'#0EA5E9'}, streaming:{enabled:true}, transparency:{showSourceCategories:true} }, features:{ sourceExplorer:true, gallery:false }, companyId: currentCompanyId },
  RPG: { key:'RPG', name:'Dungeon Master', style:'Fantasy', traits:{ themeColor:'#A78BFA', avatarUrl:"url('https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=256&q=80')" }, uiHints:{ theming:{primaryColor:'#A78BFA'}, streaming:{enabled:true}, transparency:{showSourceCategories:true} }, features:{ sourceExplorer:false, gallery:true }, companyId: currentCompanyId },
  Onboarding: { key:'Onboarding', name:'Onboarding Coach', style:'Calm', traits:{ themeColor:'#22C55E', avatarUrl:"url('https://images.unsplash.com/photo-1502685104226-ee32379fefbe?w=256&q=80')" }, uiHints:{ theming:{primaryColor:'#22C55E'}, streaming:{enabled:true}, transparency:{showSourceCategories:true} }, features:{ sourceExplorer:false, gallery:false }, companyId: currentCompanyId }
};

const $ = (q)=>document.querySelector(q);
const chatStream = $('#chatStream');
const personaName = $('#personaName');
const personaStyle = $('#personaStyle');
const themeLabel = $('#themeLabel');
const companyIdLabel = $('#companyIdLabel');
const hintsLabel = $('#hintsLabel');
const thresholdLabel = $('#thresholdLabel');
const modelLabel = $('#modelLabel');
const minSimilarityInput = $('#minSimilarity');
const minSimilarityLabel = $('#minSimilarityLabel');
const modelSelect = $('#modelSelect');
const rpgGallery = $('#rpgGallery');
const rpgGrid = $('#rpgGrid');

function applyPersonaBranding(persona){
  document.documentElement.style.setProperty('--primary-color', persona.traits.themeColor);
  document.documentElement.style.setProperty('--avatar-url', persona.traits.avatarUrl);
  personaName.textContent = persona.name;
  personaStyle.textContent = persona.style;
  themeLabel.textContent = persona.traits.themeColor;
  companyIdLabel.textContent = persona.companyId || '—';
  rpgGallery.classList.toggle('hidden', !persona.features.gallery);
  const ok = !!(persona.uiHints?.theming?.primaryColor) && !!(persona.uiHints?.streaming?.enabled) && !!(persona.uiHints?.transparency?.showSourceCategories) && !!persona.companyId;
  hintsLabel.textContent = ok ? 'OK' : 'WARN';
  hintsLabel.className = ok ? 'text-green-400' : 'text-amber-400';
  resetChat();
}

window.selectApp = (key)=>{
  const persona = PERSONAS[key];
  if(!persona) return;
  applyPersonaBranding(persona);
  modelLabel.textContent = modelSelect.value;
  thresholdLabel.textContent = Number(minSimilarityInput.value).toFixed(2);
  simulateRagUpdate();
  if(persona.features.gallery) refreshRpgGallery();
};

window.simulateRagUpdate = ()=>{
  const minScore = Number(minSimilarityInput.value);
  const matches = [
    { id:'kb_421', category:'Policies', score:0.81 },
    { id:'kb_122', category:'API', score:0.73 },
    { id:'kb_222', category:'Runbook', score:0.66 }
  ];
  const container = $('#ragMatches');
  container.innerHTML = '';
  matches.forEach(m=>{
    const warn = m.score < minScore;
    const card = document.createElement('div');
    card.className = 'rounded-lg bg-slate-900/70 border border-white/10 p-3';
    card.innerHTML = `
      <div class="flex items-center justify-between mb-1">
        <div class="text-sm font-medium">${m.id} · ${m.category}</div>
        <div class="text-xs ${warn?'text-amber-400':'text-slate-400'}">${m.score.toFixed(2)}</div>
      </div>
      <div class="h-2 w-full bg-slate-800 rounded"><div class="h-2 ${warn?'bg-amber-400':'bg-[var(--primary-color)]'} rounded" style="width:${(m.score*100).toFixed(0)}%"></div></div>
      <div class="mt-1 text-[11px] text-slate-400">${warn?'[LOW_RELEVANCE_WARNING]':'[VECTOR_MATCH]'} ${m.id}</div>
      <div class="mt-2 flex items-center gap-2">
        <button class="text-xs underline" onclick="openSource('${m.id}')">Volltext anzeigen</button>
        <span class="text-[11px] text-slate-500">${m.category}</span>
      </div>`;
    container.appendChild(card);
  });
};

window.openSource = (id)=>{
  $('#modalTitle').textContent = `Quelle: ${id}`;
  $('#modalBody').textContent = `Demo: Volltext für ${id}`;
  $('#modal').classList.remove('hidden');
};
window.closeModal = ()=> $('#modal').classList.add('hidden');

window.refreshRpgGallery = ()=>{
  rpgGrid.innerHTML = '';
  const demo = [
    { url:'https://images.unsplash.com/photo-1604079628040-94301bb21b91?w=512&q=80', prompt:'Ancient dungeon gate' },
    { url:'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=512&q=80', prompt:'Misty valley' },
    { url:'https://images.unsplash.com/photo-1549880338-65ddcdfd017b?w=512&q=80', prompt:'Crystal cavern' },
    { url:'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=512&q=80', prompt:'Arcane library' }
  ];
  demo.forEach(item=>{
    const el = document.createElement('article');
    el.className='relative overflow-hidden rounded-xl border border-white/10 bg-slate-900/60';
    el.innerHTML = `<img src="${item.url}" alt="${item.prompt}" class="w-full h-full object-cover"/><div class="absolute inset-x-0 bottom-0 p-2 bg-gradient-to-t from-black/60 to-transparent text-[11px]">${item.prompt}</div>`;
    rpgGrid.appendChild(el);
  });
};

function resetChat(){
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

window.addEventListener('DOMContentLoaded',()=> selectApp('IHK'));
