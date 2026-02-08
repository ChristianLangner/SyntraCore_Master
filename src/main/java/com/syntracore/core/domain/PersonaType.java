// Autor: Christian Langner
package com.syntracore.core.domain;

/**
 * Enum für Persona-Typen im Core-Domain.
 *
 * <p><strong>Architektur-Schicht:</strong> Domain-Modell (Hexagonal Core)</p>
 * <p><strong>Framework-Unabhängigkeit:</strong> Keine Spring/JPA-Abhängigkeiten</p>
 *
 * <h2>Persona-Typen:</h2>
 * <ul>
 *   <li><strong>SUPPORT:</strong> Professioneller Support-Bot für Kundenanfragen.
 *       Fokus auf sachliche, hilfreiche Antworten. Strikte Content-Policies.</li>
 *   <li><strong>COMPANION:</strong> Persönlicher Companion-Bot mit flexiblen
 *       Interaktionsmöglichkeiten. Unterstützt unterschiedliche Freizügigkeitsgrade.</li>
 * </ul>
 *
 * <h2>Verwendung:</h2>
 * <p>Steuert das Verhalten der Persona und die erlaubten Interaktionsformen.
 * Der Typ bestimmt auch, welche Safety-Level bei der Bildgenerierung zulässig sind.</p>
 *
 * @author Christian Langner
 * @version 1.0
 * @since 2026
 *
 * @see Persona
 */
public enum PersonaType {

    /**
     * Professioneller Support-Bot.
     * Fokus auf sachliche Hilfestellung und Problemlösung.
     */
    SUPPORT,

    /**
     * Persönlicher Companion-Bot.
     * Flexiblere Interaktionsmöglichkeiten mit konfigurierbarem Content-Level.
     */
    COMPANION
}
