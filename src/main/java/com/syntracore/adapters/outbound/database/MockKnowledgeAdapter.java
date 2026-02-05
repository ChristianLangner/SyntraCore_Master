package com.syntracore.adapters.outbound.database;

import com.syntracore.core.ports.KnowledgeBasePort;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock-Adapter für die Wissensdatenbank (temporäre Implementierung).
 * 
 * <p>Diese Klasse ist ein <strong>Outbound-Adapter</strong> in der Hexagonalen Architektur
 * und implementiert das {@link KnowledgeBasePort}-Interface. Sie simuliert eine
 * Wissensdatenbank mit <strong>hart-codierten Testdaten</strong>.</p>
 * 
 * <h2>Warum ist dieser Mock-Adapter wichtig?</h2>
 * <ul>
 *   <li><strong>Entwicklung ohne Infrastruktur:</strong> Ermöglicht RAG-Tests ohne echte
 *       Vektordatenbank oder Cloud-Anbindung.</li>
 *   <li><strong>Firewall-Umgehung:</strong> Funktioniert auch in Netzwerken mit
 *       eingeschränktem Internetzugriff (z.B. Schul-Netzwerk).</li>
 *   <li><strong>Schnelle Iteration:</strong> Keine Setup-Zeit für Datenbank-Infrastruktur.</li>
 *   <li><strong>Deterministisches Verhalten:</strong> Immer gleiche Ergebnisse für Tests.</li>
 * </ul>
 * 
 * <h2>Architektur-Kontext:</h2>
 * <pre>
 * Domain Layer → KnowledgeBasePort (Interface) → MockKnowledgeAdapter (hier)
 * 
 * Zukünftig:
 * Domain Layer → KnowledgeBasePort → VectorDatabaseAdapter → Pinecone/Weaviate
 * </pre>
 * 
 * <h2>Funktionsweise:</h2>
 * <p>Der Adapter durchsucht die Query nach Schlüsselwörtern und gibt passende
 * Handbuch-Einträge zurück. Dies simuliert eine semantische Suche.</p>
 * 
 * <h2>Beispiel-Daten:</h2>
 * <ul>
 *   <li><strong>"passwort"/"login"</strong> → Passwort-Reset-Anleitung</li>
 *   <li><strong>"rechnung"/"bezahlen"</strong> → Zahlungsinformationen</li>
 *   <li><strong>Sonstiges</strong> → Allgemeiner Support-Hinweis</li>
 * </ul>
 * 
 * <p><strong>⚠️ Hinweis:</strong> Dies ist eine <strong>temporäre Lösung</strong> für die
 * Entwicklungsphase. In Produktion sollte eine echte Vektordatenbank verwendet werden
 * (z.B. Pinecone, Weaviate, Qdrant) für semantische Suche.</p>
 * 
 * @author SyntraCore Development Team
 * @version 2.0
 * @since 2.0
 * 
 * @see com.syntracore.core.ports.KnowledgeBasePort
 * @see com.syntracore.core.services.TicketService
 */
@Component
public class MockKnowledgeAdapter implements KnowledgeBasePort {

    /**
     * Sucht nach relevanten Informationen in der simulierten Wissensdatenbank.
     * 
     * <p>Diese Methode implementiert eine <strong>einfache Keyword-basierte Suche</strong>
     * durch String-Matching. In einer echten Implementierung würde hier eine
     * semantische Vektorsuche stattfinden.</p>
     * 
     * <h3>Ablauf:</h3>
     * <ol>
     *   <li>Query wird in Kleinbuchstaben konvertiert (case-insensitive)</li>
     *   <li>Schlüsselwörter werden gesucht</li>
     *   <li>Passende Handbuch-Einträge werden zurückgegeben</li>
     * </ol>
     * 
     * <h3>Unterstützte Themen:</h3>
     * <table border="1">
     *   <tr>
     *     <th>Schlüsselwörter</th>
     *     <th>Handbuch-Eintrag</th>
     *   </tr>
     *   <tr>
     *     <td>passwort, login</td>
     *     <td>Passwort-Reset-Anleitung</td>
     *   </tr>
     *   <tr>
     *     <td>rechnung, bezahlen</td>
     *     <td>Zahlungsinformationen</td>
     *   </tr>
     *   <tr>
     *     <td>Andere</td>
     *     <td>Allgemeiner Support-Hinweis</td>
     *   </tr>
     * </table>
     * 
     * <p><strong>Verwendungsbeispiel:</strong></p>
     * <pre>
     * List&lt;String&gt; context = mockAdapter.findRelevantContext("Passwort vergessen");
     * // Ergebnis: ["HANDBUCH-INFO: Passwort-Resets können über das Portal..."]
     * </pre>
     * 
     * @param query Die Suchanfrage (typischerweise die Ticket-Nachricht)
     * 
     * @return Eine Liste mit passenden Handbuch-Einträgen. Niemals null,
     *         aber kann leer sein (aktuell wird immer mindestens ein Eintrag zurückgegeben).
     * 
     * @throws NullPointerException wenn query null ist
     */
    @Override
    public List<String> findRelevantContext(String query) {
        List<String> mockData = new ArrayList<>();

        // Keyword-basierte Suche (simuliert semantische Suche)
        // In Produktion würde hier eine Vektorsuche stattfinden
        
        if (query.toLowerCase().contains("passwort") || query.toLowerCase().contains("login")) {
            // Passwort/Login-bezogene Anfragen
            mockData.add("HANDBUCH-INFO: Passwort-Resets können über das Portal unter 'Sicherheit' angefordert werden.");
        } else if (query.toLowerCase().contains("rechnung") || query.toLowerCase().contains("bezahlen")) {
            // Rechnungs-/Zahlungsbezogene Anfragen
            mockData.add("HANDBUCH-INFO: Rechnungen sind innerhalb von 14 Tagen per Banküberweisung zu begleichen.");
        } else {
            // Fallback für alle anderen Anfragen
            mockData.add("HANDBUCH-INFO: Allgemeiner Support-Hinweis: Bitte immer die Kundennummer angeben.");
        }

        return mockData;
    }
}