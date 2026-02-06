// UPDATE #1
// Neue Domain-Entität für die Mandantenfähigkeit (Multi-Tenancy).
// Zweck: Zentraler Anker für alle Daten eines spezifischen Kunden.
package com.syntracore.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Domain-Modell für eine Firma (Mandant).
 * In einer hexagonalen Architektur ist dies ein reines POJO ohne Framework-Bindung.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company {

    /** Eindeutige ID der Firma */
    private UUID id;

    /** Name der Firma */
    private String name;

    /**
     * Konstruktor für die Neuerstellung einer Firma.
     * @param name Name des neuen Mandanten
     */
    public Company(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }
}