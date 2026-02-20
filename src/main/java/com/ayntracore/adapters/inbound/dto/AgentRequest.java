package com.ayntracore.adapters.inbound.dto;

import lombok.Data;

/**
 * Universal Data Transfer Object for requests to the AgentController.
 * It includes fields for different modes like RAG, knowledge entry creation, and more.
 */
@Data
public class AgentRequest {
    private String companyId;
    private String mode;

    // Renamed from 'query' to align with frontend
    private String message;

    // Fields for Knowledge Entry creation (e.g., mode = "knowledge")
    private String category;
    private String content;
    private String source;

    // Placeholder for image-related modes
    private String imageUrl;
}
