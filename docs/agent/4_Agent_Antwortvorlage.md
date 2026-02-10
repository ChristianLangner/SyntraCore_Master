
# Antwortvorlage für den Agenten

```markdown
**Kurzlösung**  
… prägnante Antwort …

**Vorgehen / RAG-Kontext (kurz)**
- Treffer: kb_421 (Policies) – 0.81
- Treffer: kb_122 (API) – 0.73 ⚠️ (unter 0.70 → nicht verwenden)

**Quellen**
- kb_421 · category: Policies

**UI-Hints (JSON)**
{
  "theming": { "primaryColor": "{{persona.traits.themeColor|default('#3B82F6')}}" },
  "streaming": { "enabled": true },
  "transparency": { "showSourceCategories": true }
}
```
