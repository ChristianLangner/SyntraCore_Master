
$(document).ready(function () {
    const PROFILES = {
        'astra': {
            id: '550e8400-e29b-41d4-a716-446655440000',
            name: 'Astra Noir',
            hasImages: false,
            model: 'google/gemini-flash-1.5',
            temp: 0.7,
            color: '#3B82F6'
        },
        'seraphina': {
            id: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
            name: 'Seraphina',
            hasImages: true,
            model: 'mistralai/mistral-7b-instruct',
            temp: 0.9,
            color: '#EF4444'
        },
        'luna': {
            id: '7f8c332b-8e5a-460f-9d12-c5762d875122',
            name: 'Luna',
            hasImages: true,
            model: 'openai/dall-e-3',
            temp: 1.0,
            color: '#06B6D4'
        }
    };

    let activeProfileKey = 'astra';
    let currentCompanyId = PROFILES[activeProfileKey].id;
    let stompClient = null;

    function switchProfile(profileKey) {
        const profile = PROFILES[profileKey];
        if (!profile) return;

        activeProfileKey = profileKey;
        currentCompanyId = profile.id;

        console.log(`[PROFILE] Switching to ${profile.name}`);

        // Update UI elements based on profile
        $('.btn-group .btn').removeClass('active');
        $(`#btn-${profileKey}`).addClass('active');
        document.documentElement.style.setProperty('--primary-color', profile.color);
        $('#persona-name').text(profile.name);

        // Update settings panel to profile defaults
        $('#model-select').val(profile.model);
        $('#temperature-slider').val(profile.temp);
        $('#temperature-value').text(profile.temp);

        // Toggle image display and clear content
        $('#image-display-area').toggle(profile.hasImages);
        $('#generated-image').attr('src', '');
        $('#chat-messages').empty();
        appendSystemMessage(`Profil gewechselt zu ${profile.name}.`);
    }

    // Event Listeners
    $('#btn-astra').click(() => switchProfile('astra'));
    $('#btn-seraphina').click(() => switchProfile('seraphina'));
    $('#btn-luna').click(() => switchProfile('luna'));
    $('#temperature-slider').on('input', (e) => $('#temperature-value').text(e.target.value));
    $('#send').click(sendMessage);
    $('#message').keypress(e => e.which === 13 && sendMessage());

    function connect() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, 
            () => {
                console.log('STOMP Connected');
                stompClient.subscribe('/topic/responses', (response) => {
                    handleServerResponse(JSON.parse(response.body));
                });
            },
            (error) => {
                console.error("STOMP connection error: " + error);
                appendSystemMessage("Verbindung zum Server fehlgeschlagen.");
            }
        );
    }

    function handleServerResponse(response) {
        if (response.shortAnswer) {
            appendMessage('assistant', response.shortAnswer);
        }
        if (response.imageUrl) {
            $('#generated-image').attr('src', response.imageUrl);
        }
        $('#image-loading-spinner').addClass('d-none');
    }

    function appendMessage(role, content) {
        if (!content) return;
        const roleLabel = role.charAt(0).toUpperCase() + role.slice(1);
        const formattedContent = content.replace(/\n/g, '<br>');
        const message = `<div class="message"><strong>${roleLabel}:</strong> ${formattedContent}</div>`;
        $('#chat-messages').append(message).scrollTop($('#chat-messages')[0].scrollHeight);
    }

    function appendSystemMessage(content) {
        const message = `<div class="message system"><em>${content}</em></div>`;
        $('#chat-messages').append(message).scrollTop($('#chat-messages')[0].scrollHeight);
    }

    function sendMessage() {
        const messageContent = $('#message').val().trim();
        if (!messageContent) return;

        appendMessage('user', messageContent);
        $('#message').val('');

        const profile = PROFILES[activeProfileKey];

        // *** EXPLICIT MODE DETERMINATION ***
        let requestMode = 'text';
        if (profile.hasImages) {
            requestMode = 'image';
            $('#generated-image').attr('src', '');
            $('#image-loading-spinner').removeClass('d-none');
        }

        const payload = {
            companyId: currentCompanyId,
            message: messageContent,
            mode: requestMode, // Using the explicitly set mode
            model: $('#model-select').val(),
            temperature: parseFloat($('#temperature-slider').val())
        };

        console.log("[API PAYLOAD] " + JSON.stringify(payload));

        $.ajax({
            url: '/api/agent/entry',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: () => console.log("Request sent, waiting for WS response."),
            error: (xhr) => {
                console.error("Error sending message:", xhr.responseText);
                appendSystemMessage(`Fehler: ${xhr.responseText || 'Server nicht erreichbar'}`);
                if (profile.hasImages) {
                    $('#image-loading-spinner').addClass('d-none');
                }
            }
        });
    }

    // Initial setup
    connect();
    switchProfile('astra');
});
