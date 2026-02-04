// UPDATE #1
// Notiz: Erster Entwurf für RAG-Simulation in der Schule ohne SQL-Verbindung.
package com.syntracore.adapters.outbound.database;

import com.syntracore.core.ports.KnowledgeBasePort;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Ein temporärer Adapter, der die Wissensdatenbank simuliert.
 * Hilft uns, die RAG-Logik zu testen, solange die Firewall die Cloud-DB blockt.
 */
@Component
public class MockKnowledgeAdapter implements KnowledgeBasePort {

    @Override
    public List<String> findRelevantContext(String query) {
        List<String> mockData = new ArrayList<>();

        // Wir simulieren einen Treffer aus dem Handbuch
        if (query.toLowerCase().contains("passwort") || query.toLowerCase().contains("login")) {
            mockData.add("HANDBUCH-INFO: Passwort-Resets können über das Portal unter 'Sicherheit' angefordert werden.");
        } else if (query.toLowerCase().contains("rechnung") || query.toLowerCase().contains("bezahlen")) {
            mockData.add("HANDBUCH-INFO: Rechnungen sind innerhalb von 14 Tagen per Banküberweisung zu begleichen.");
        } else {
            mockData.add("HANDBUCH-INFO: Allgemeiner Support-Hinweis: Bitte immer die Kundennummer angeben.");
        }

        return mockData;
    }
}