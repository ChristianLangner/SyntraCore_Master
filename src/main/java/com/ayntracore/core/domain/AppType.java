package com.ayntracore.core.domain;

/**
 * Defines the application preset scenarios for persona configuration.
 */
public enum AppType {
    /**
     * A role-playing game persona, like a Dungeon Master.
     * Characteristics: Emotional style, companion type, image generation enabled.
     */
    RPG,

    /**
     * An expert persona for professional contexts, like a certified expert.
     * Characteristics: Professional style, support type, focus on technical context.
     */
    IHK_EXPERT,

    /**
     * A persona designed to help new developers with onboarding.
     * Characteristics: Supportive style, focus on codebase and development processes.
     */
    DEV_ONBOARDING
}
