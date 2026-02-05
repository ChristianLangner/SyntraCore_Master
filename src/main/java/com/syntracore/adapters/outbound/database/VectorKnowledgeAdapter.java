// UPDATE #16: Vorbereitung für echte Vektor-Datenbank (z.B. Qdrant/Pinecone)
// Ort: src/main/java/com/syntracore/adapters/outbound/database/VectorKnowledgeAdapter.java

package com.syntracore.adapters.outbound.database;

import com.syntracore.core.ports.KnowledgeBasePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Profile("cloud") // Nur aktiv bei spring.profiles.active=cloud
public class VectorKnowledgeAdapter implements KnowledgeBasePort {

    @Override
    public List<String> findRelevantContext(String query) {
        // HIER würde die REST-API Anfrage an Qdrant/Pinecone stehen
        // Beispiel: return qdrantClient.search(queryVectors);
        return List.of("Cloud-Vektor-Suche simuliert: Hier käme das Wissen aus der Cloud.");
    }
}