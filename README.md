# regulafin-rag

RAG (Retrieval-Augmented Generation) assistant for Brazilian financial regulation, built for fintech engineers and product teams.

## What it does

Answers questions about Brazilian banking regulations (currently: Resolução BCB 4.658/2018 — cybersecurity and cloud computing requirements for financial institutions) with citations to specific articles, paragraphs, and clauses — instead of hallucinating plausible-sounding answers.

## Why it matters

Financial regulators publish dense, legally binding documents that engineers and product teams need to understand to build compliant fintech products. ChatGPT-style answers are often plausible but unverifiable. This system grounds every answer on the actual regulation, with traceable citations, and refuses to answer when the information isn't in the indexed corpus.

## Stack

- **Java 21 + Spring Boot 3.5** — backend
- **Spring AI 1.1** — LLM orchestration, embeddings, vector store integration
- **OpenAI** — `gpt-4o-mini` for chat, `text-embedding-3-small` for embeddings
- **PostgreSQL 16 + pgvector** — vector store (HNSW index, cosine distance)
- **Docker Compose** — local infrastructure

## Architecture (current)

```
PDF → PagePdfDocumentReader → TokenTextSplitter → OpenAI Embeddings → pgvector
                                                                          ↓
User question → Embedding → Similarity search (top-K) → QuestionAnswerAdvisor → LLM → Answer
```

## Endpoints

- `GET /ping` — health check
- `GET /chat?q=...` — vanilla LLM (no RAG, baseline for comparison)
- `GET /ask?q=...` — RAG-powered Q&A grounded on indexed regulations

## Demo

Question: *"Quais são os requisitos para contratar serviços de computação em nuvem segundo a Resolução 4658?"*

- `/chat` (no RAG): generic plausible answer, no citations.
- `/ask` (with RAG): cites Art. 12 §1º, §2º, §4º, and incisos d, e, f, g, h.

## Local setup

```bash
git clone <this-repo>
cd regulafin-rag

# Set OpenAI key
echo "OPENAI_API_KEY=sk-..." > .env

# Start Postgres + pgvector
docker compose up -d

# Run app
./mvnw spring-boot:run
```

The app ingests the PDF on first startup and is idempotent (won't re-ingest on restart).

## Roadmap

- [x] Day 0: Spring Boot baseline with OpenAI and pgvector
- [x] Day 1: First working RAG with one regulation
- [ ] Day 2: Source attribution (return retrieved chunks alongside answers)
- [ ] Day 3: Smarter chunking (by article/section)
- [ ] Day 4: Multi-document support (add Res. 4.966)
- [ ] Day 5: Conversational memory
- [ ] Day 6: Automated evaluation (RAGAs-style metrics)
- [ ] Day 7+: Reranking, observability, frontend

## Why this project exists

Built as part of a portfolio targeting AI Engineer roles at Brazilian fintechs (Sicredi, Nubank, Stone, Creditas, etc.), where deep knowledge of Brazilian financial regulation is a real differentiator. Combines backend engineering (Spring/Java) with practical AI engineering (RAG, embeddings, evaluation).