// Autor: Christian Langner
package com.ayntracore.core.ports;

import com.ayntracore.core.domain.AiChatRequest;
import com.ayntracore.core.domain.AiResponse;

import java.util.concurrent.Flow;

/**
 * Universeller AI-Port zur Abstraktion verschiedener LLM-Provider.
 * Definiert Methoden für synchrone und streaming-basierte Antworten.
 */
public interface UniversalAiPort {

    /**
     * Generiert eine vollständige Antwort für einen Chat-Request.
     *
     * @param request Die Anfrage-Parameter
     * @return Die Antwort inkl. Metadaten
     */
    AiResponse generateResponse(AiChatRequest request);

    /**
     * Generiert eine Streaming-Antwort für WebSockets oder reaktive Frontends.
     * Verwendet Java Flow API (Publisher) für reaktive Abstraktion ohne externe Libs.
     *
     * @param request Die Anfrage-Parameter
     * @return Ein Publisher, der Antwort-Fragmente (Tokens) emittiert
     */
    Flow.Publisher<AiResponse> generateStreamingResponse(AiChatRequest request);
}
