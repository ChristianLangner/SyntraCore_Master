package com.ayntracore.adapters.inbound;

import com.ayntracore.adapters.inbound.dto.*;
import com.ayntracore.core.application.ImageGenerationService;
import com.ayntracore.core.application.RAGCoordinationService;
import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.ports.ImageGenerationPort;
import com.ayntracore.core.ports.PersonaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private final PersonaRepositoryPort personaRepository;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.70;

    @PostMapping("/entry")
    public ResponseEntity<?> entryPoint(@RequestBody AgentRequest request) {
        UUID companyId;
        try {
            companyId = UUID.fromString(request.getCompanyId());
        } catch (IllegalArgumentException e) {
            log.error("[SECURITY] Invalid companyId format: {}", request.getCompanyId());
            return ResponseEntity.badRequest().body("Invalid companyId format.");
        }

        String mode = request.getMode();
        if (mode == null || mode.equalsIgnoreCase("initial") || mode.equalsIgnoreCase("INITIALIZATION")) {
            mode = "text";
        }

        switch (mode) {
            case "text":
                return handleTextMode(request, companyId);
            case "image":
                return handleImageMode(request, companyId);
            default:
                log.warn("[SECURITY] Invalid mode specified: {}", request.getMode());
                return ResponseEntity.badRequest().body("Invalid mode.");
        }
    }

    private ResponseEntity<AgentResponse> handleTextMode(AgentRequest request, UUID companyId) {
        Persona persona;
        try {
            persona = personaRepository.findActiveByCompanyId(companyId)
                    .orElseThrow(() -> new RuntimeException("No active persona found for company: " + companyId));
        } catch (Exception e) {
            log.warn("[FALLBACK] Persona not found for companyId {}. Returning Ayntra Guardian fallback. Reason: {}", companyId, e.getMessage());

            UiHints uiHints = UiHints.builder()
                    .primaryColor("#FF0000") // Red for error/fallback
                    .theme("dark")
                    .personaName("Ayntra Guardian")
                    .build();

            AgentResponse agentResponse = AgentResponse.builder()
                    .shortAnswer("Die angeforderte Persona konnte nicht geladen werden oder es ist keine als 'aktiv' markiert. Der Ayntra Guardian ist als Platzhalter eingesprungen.")
                    .sources(List.of())
                    .uiHints(uiHints)
                    .build();

            return ResponseEntity.ok(agentResponse);
        }

        RAGCoordinationService.RAGResponse ragResponse = ragService.generateResponseWithContextAdvanced(
                request.getMessage(),
                persona,
                5, // contextLimit
                MIN_SIMILARITY_THRESHOLD
        );

        List<Source> sources = ragResponse.usedContexts().stream()
                .filter(context -> {
                    if (context.similarity() < MIN_SIMILARITY_THRESHOLD) {
                        log.warn("[QUALITY] LOW_RELEVANCE_WARNING: Source {} has similarity {} which is below the threshold of {}", context.source(), context.similarity(), MIN_SIMILARITY_THRESHOLD);
                        return false;
                    }
                    return true;
                })
                .map(context -> Source.builder()
                        .sourceName(context.source())
                        .relevance(context.similarity())
                        .link(null) // Link is not available in the context metadata
                        .build())
                .collect(Collectors.toList());

        UiHints uiHints = UiHints.builder()
                .primaryColor(persona.getTraits().get("primaryColor"))
                .theme(persona.getTraits().get("theme"))
                .personaName(persona.getName())
                .build();

        AgentResponse agentResponse = AgentResponse.builder()
                .shortAnswer(ragResponse.llmResponse())
                .longAnswer(null) // Not yet implemented
                .sources(sources)
                .uiHints(uiHints)
                .build();

        return ResponseEntity.ok(agentResponse);
    }

    private ResponseEntity<AgentResponse> handleImageMode(AgentRequest request, UUID companyId) {
        Persona persona;
        try {
            persona = personaRepository.findActiveByCompanyId(companyId)
                    .orElseThrow(() -> new RuntimeException("No active persona found for company: " + companyId));
        } catch (Exception e) {
            log.warn("[FALLBACK] Persona not found for companyId {}. Returning Ayntra Guardian fallback for image mode. Reason: {}", companyId, e.getMessage());

            UiHints uiHints = UiHints.builder()
                    .primaryColor("#FF0000")
                    .theme("dark")
                    .personaName("Ayntra Guardian")
                    .build();

            AgentResponse agentResponse = AgentResponse.builder()
                    .imageUrl(null)
                    .shortAnswer("Bild-Persona nicht gefunden. Der Ayntra Guardian ist als Platzhalter eingesprungen.")
                    .uiHints(uiHints)
                    .build();

            return ResponseEntity.ok(agentResponse);
        }

        ImageGenerationPort.ImageGenerationResponse imageResponse = imageGenerationService.generateImageAdvanced(
                request.getMessage(),
                persona,
                "low quality, blurry, distorted", // negative prompt
                512, // width
                512, // height
                30, // steps
                persona.getTraits().get("modelId")
        );

        UiHints uiHints = UiHints.builder()
                .primaryColor(persona.getTraits().get("primaryColor"))
                .theme(persona.getTraits().get("theme"))
                .personaName(persona.getName())
                .build();

        AgentResponse agentResponse = AgentResponse.builder()
                .imageUrl(imageResponse.imageUrl())
                .uiHints(uiHints)
                .build();

        return ResponseEntity.ok(agentResponse);
    }
}
