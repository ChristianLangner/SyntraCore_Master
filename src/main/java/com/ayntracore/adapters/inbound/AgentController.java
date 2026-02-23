package com.ayntracore.adapters.inbound;

import com.ayntracore.adapters.inbound.dto.*;
import com.ayntracore.core.application.ImageGenerationService;
import com.ayntracore.core.application.RAGCoordinationService;
import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.ports.ImageGenerationPort;
import com.ayntracore.core.ports.PersonaOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class AgentController {

    private final RAGCoordinationService ragService;
    private final ImageGenerationService imageGenerationService;
    private final PersonaOutputPort personaOutputPort;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.5;

    @PostMapping("/entry")
    public ResponseEntity<?> entryPoint(@RequestBody AgentRequest request) {
        UUID companyId;
        try {
            companyId = UUID.fromString(request.getCompanyId());
        } catch (IllegalArgumentException e) {
            log.error("[SECURITY] Invalid companyId format: {}", request.getCompanyId());
            return ResponseEntity.badRequest().body("Invalid companyId format.");
        }

        String mode = request.getMode() != null ? request.getMode() : "text";

        // Find persona first, as it's needed in all modes and for traits.
        Optional<Persona> personaOpt = personaOutputPort.findActiveByCompanyId(companyId);
        if (personaOpt.isEmpty()) {
            return buildFallbackResponse(companyId);
        }
        Persona persona = personaOpt.get();

        // Override model and temperature if provided in request, otherwise use persona traits.
        String model = request.getModel() != null ? request.getModel() : persona.getTraits().get("model");
        Double temperature = request.getTemperature() != null ? request.getTemperature() : Double.parseDouble(persona.getTraits().getOrDefault("temp", "0.7"));

        log.info("[REQUEST] Mode: '{}', Model: '{}', Temp: {}", mode, model, temperature);

        switch (mode) {
            case "text":
                return handleTextMode(request, persona, model, temperature);
            case "image":
                return handleImageMode(request, persona, model, temperature);
            default:
                log.warn("[SECURITY] Invalid mode specified: {}", request.getMode());
                return ResponseEntity.badRequest().body("Invalid mode.");
        }
    }

    private ResponseEntity<AgentResponse> handleTextMode(AgentRequest request, Persona persona, String model, Double temperature) {
        RAGCoordinationService.RAGResponse ragResponse = ragService.generateResponseWithContextAdvanced(
                request.getMessage(),
                persona,
                5, // contextLimit
                MIN_SIMILARITY_THRESHOLD,
                model,
                temperature
        );

        List<Source> sources = ragResponse.usedContexts().stream()
                .map(context -> Source.builder()
                        .sourceName(context.source())
                        .relevance(context.similarity())
                        .build())
                .collect(Collectors.toList());

        UiHints uiHints = buildUiHints(persona);
        AgentResponse agentResponse = AgentResponse.builder()
                .shortAnswer(ragResponse.llmResponse())
                .sources(sources)
                .uiHints(uiHints)
                .build();

        return ResponseEntity.ok(agentResponse);
    }

    private ResponseEntity<AgentResponse> handleImageMode(AgentRequest request, Persona persona, String model, Double temperature) {
        ImageGenerationPort.ImageGenerationResponse imageResponse = imageGenerationService.generateImageAdvanced(
                request.getMessage(),
                persona,
                "low quality, blurry, distorted, ugly", // Universal negative prompt
                1024, // width
                1024, // height
                20,   // steps
                model  // Dynamic model from request/persona
        );

        UiHints uiHints = buildUiHints(persona);
        AgentResponse agentResponse = AgentResponse.builder()
                .imageUrl(imageResponse.imageUrl())
                .uiHints(uiHints)
                .build();

        return ResponseEntity.ok(agentResponse);
    }

    private UiHints buildUiHints(Persona persona) {
        return UiHints.builder()
                .primaryColor(persona.getTraits().get("primaryColor"))
                .theme(persona.getTraits().get("theme"))
                .personaName(persona.getName())
                .build();
    }

    private ResponseEntity<AgentResponse> buildFallbackResponse(UUID companyId) {
        log.warn("[FALLBACK] Persona not found for companyId {}. Returning Ayntra Guardian fallback.", companyId);
        UiHints uiHints = UiHints.builder()
                .primaryColor("#FF0000")
                .theme("dark")
                .personaName("Ayntra Guardian")
                .build();
        AgentResponse agentResponse = AgentResponse.builder()
                .shortAnswer("Die angeforderte Persona konnte nicht geladen werden. Der Ayntra Guardian ist als Platzhalter eingesprungen.")
                .uiHints(uiHints)
                .build();
        return ResponseEntity.ok(agentResponse);
    }
}
