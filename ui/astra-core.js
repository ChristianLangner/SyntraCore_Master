const PERSONA_UUID = "550e8400-e29b-41d4-a716-446655440000";
let socket;

document.addEventListener('DOMContentLoaded', () => {
    initConnection();
    fetchPersonaData();

    const sendBtn = document.getElementById('sendBtn');
    const chatInput = document.getElementById('chatInput');

    sendBtn.addEventListener('click', handleSendMessage);
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            handleSendMessage();
        }
    });
});

function initConnection() {
    const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    socket = new WebSocket(`${proto}//${window.location.host}/ws/chat`);
    socket.onmessage = onSocketMessage;
    socket.onopen = () => showToast("Astra ist online.");
    socket.onerror = () => showToast("Verbindung zu Astra unterbrochen.");
}

function onSocketMessage(event) {
    console.log("RAW_RESPONSE:", event.data);
    const data = JSON.parse(event.data);

    if (data.type === 'image') {
        const imgElement = processAstraImage(data.payload);
        if (imgElement) {
            document.getElementById('chatStream').appendChild(imgElement);
        }
    } else if (data.type === 'message') {
         appendChatMessage('Astra', data.payload);
    }
}

function handleSendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    if (message) {
        appendChatMessage('User', message);
        socket.send(JSON.stringify({
            message: message,
            personaId: PERSONA_UUID,
            companyId: PERSONA_UUID
        }));
        input.value = '';
    }
}

function appendChatMessage(sender, message) {
    const stream = document.getElementById('chatStream');
    const messageElement = document.createElement('div');
    messageElement.innerHTML = `<strong>${sender}:</strong> ${message}`;
    stream.appendChild(messageElement);
    stream.scrollTop = stream.scrollHeight;
}

function processAstraImage(responsePayload) {
    let finalUrl = "";
    
    console.log("Processing image payload:", responsePayload);

    if (typeof responsePayload === 'string' && responsePayload.startsWith('data:image')) {
        console.log("Astra: Base64 Image erkannt.");
        finalUrl = responsePayload;
    } 
    else if (typeof responsePayload === 'string' && responsePayload.trim().startsWith('{')) {
        try {
            const parsed = JSON.parse(responsePayload);
            finalUrl = parsed.data?.[0]?.url || parsed.url;
        } catch(e) {
            console.error("Astra: JSON Parsing fehlgeschlagen", e);
        }
    } 
    else if (typeof responsePayload === 'object' && responsePayload !== null) {
        finalUrl = responsePayload.data?.[0]?.url || responsePayload.url;
    }

    if (finalUrl) {
        const img = document.createElement('img');
        img.src = finalUrl;
        img.className = "chat-image";
        return img;
    }
    console.error("Astra: Konnte keine valide Bild-URL extrahieren.");
    return null;
}

async function fetchPersonaData() {
    try {
        const response = await fetch('/api/agent/entry', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                personaId: PERSONA_UUID,
                companyId: PERSONA_UUID,
                message: "Ayntra Initialization"
            })
        });
        
        if (!response.ok) {
            throw new Error(`API-Fehler: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        if(data.uiHints) {
            applyUiHints(data.uiHints);
        }

    } catch (error) {
        console.error("Fehler bei der Initialisierung:", error);
        showToast(error.message);
        applyUiHints({ primaryColor: '#FFA500', personaName: 'Ayntra (Offline)' });
    }
}

function applyUiHints(hints) {
    const primaryColor = hints.primaryColor || '#FFA500';
    const personaName = hints.personaName || 'Executive';
    const primaryRgb = primaryColor.match(/\w\w/g).map(x => parseInt(x, 16));


    document.documentElement.style.setProperty('--primary-color', primaryColor);
    document.documentElement.style.setProperty('--primary-color-rgb', `${primaryRgb[0]}, ${primaryRgb[1]}, ${primaryRgb[2]}`);
    document.getElementById('persona-name').textContent = personaName;
}

function showToast(message) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = 'toast-message';
    toast.textContent = message;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    }, 100);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 5000);
}
