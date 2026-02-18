// Autor: Christian Langner
package com.ayntracore.core.ports;

import com.pgvector.PGvector;

/**
 * Port für die Erstellung von Vektor-Embeddings.
 */
public interface EmbeddingPort {

    /**
     * Erstellt ein Embedding für den gegebenen Text.
     *
     * @param text Der Text, für den ein Embedding erstellt werden soll
     * @return Das erstellte Embedding
     */
    PGvector createEmbedding(String text);
}
