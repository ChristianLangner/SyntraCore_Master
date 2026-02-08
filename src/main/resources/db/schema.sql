-- Autor: Christian Langner
-- SQL-Schema für SyntraCore: Persona-Erweiterung und RAG-Vektor-Integration
-- Datenbank: Neon PostgreSQL mit pgvector Extension

-- ============================================================
-- 1. Extension für Vektor-Support aktivieren
-- ============================================================
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================
-- 2. Persona-Tabelle
-- ============================================================
CREATE TABLE IF NOT EXISTS persona (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    persona_type VARCHAR(50) NOT NULL CHECK (persona_type IN ('SUPPORT', 'COMPANION')),
    allow_explicit_content BOOLEAN NOT NULL DEFAULT FALSE,
    system_prompt TEXT,
    speaking_style VARCHAR(500),
    traits JSONB,
    prompt_template TEXT,
    example_dialog TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_company_persona UNIQUE (company_id)
);

-- Index für schnelle Company-Lookups
CREATE INDEX IF NOT EXISTS idx_persona_company_id ON persona(company_id);

-- ============================================================
-- 3. Knowledge-Tabelle mit Vektor-Embedding
-- ============================================================
CREATE TABLE IF NOT EXISTS knowledge_base (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(255),
    source VARCHAR(500),
    embedding vector(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index für schnelle Company-Lookups
CREATE INDEX IF NOT EXISTS idx_knowledge_company_id ON knowledge_base(company_id);

-- Index für Vektor-Ähnlichkeitssuche (HNSW für bessere Performance)
CREATE INDEX IF NOT EXISTS idx_knowledge_embedding ON knowledge_base
USING hnsw (embedding vector_cosine_ops);

-- ============================================================
-- 4. Trigger für automatisches Update von updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_persona_updated_at
    BEFORE UPDATE ON persona
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_knowledge_updated_at
    BEFORE UPDATE ON knowledge_base
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 5. Beispiel-Abfrage für Vektor-Ähnlichkeitssuche
-- ============================================================
-- SELECT id, content, 1 - (embedding <=> '[0.1, 0.2, ...]'::vector) AS similarity
-- FROM knowledge_base
-- WHERE company_id = '...'
-- ORDER BY embedding <=> '[0.1, 0.2, ...]'::vector
-- LIMIT 5;
