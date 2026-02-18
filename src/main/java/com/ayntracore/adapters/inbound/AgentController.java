package com.ayntracore.adapters.inbound;

import com.ayntracore.adapters.inbound.dto.*;
import com.ayntracore.core.application.RAGCoordinationService;
import com.ayntracore.core.domain.Persona;
import com.ayntracore.core.ports.PersonaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final RAGCoordinationService ragService;
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

        switch (request.getMode()) {
            case "text":
                return handleTextMode(request, companyId);
            case "image":
                // Placeholder for Face-Lock logic
                return ResponseEntity.ok().body("Image mode not yet implemented.");
            default:
                return ResponseEntity.badRequest().body("Invalid mode.");
        }
    }

    private ResponseEntity<AgentResponse> handleTextMode(AgentRequest request, UUID companyId) {
        Persona persona = personaRepository.findActiveByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("No active persona for this company"));

        RAGCoordinationService.RAGResponse ragResponse = ragService.generateResponseWithContextAdvanced(
                request.getQuery(),
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
}
