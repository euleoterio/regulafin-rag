package com.regulafin.assistant.service;

import com.regulafin.assistant.dto.AskResponse;
import com.regulafin.assistant.dto.SourceDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final int TOP_K = 4;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    private static final String SYSTEM_PROMPT = """
            Você é um assistente especializado em regulação financeira brasileira.
            Sua audiência é composta por engenheiros e times de produto de fintechs.

            Regras estritas:
            1. Responda APENAS com base no CONTEXTO fornecido abaixo.
            2. Se a resposta não estiver no contexto, diga claramente:
               "Não encontrei essa informação nos normativos indexados."
               NÃO invente, NÃO use conhecimento geral.
            3. Sempre que possível, cite o artigo, parágrafo ou inciso do normativo.
            4. Use linguagem técnica clara, evite juridiquês desnecessário.
            5. Se a pergunta for ambígua, peça esclarecimento.
            """;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(buildSearchRequest())
                                .build()
                )
                .build();
    }

    public AskResponse ask(String question) {
        // 1. Recupera os chunks manualmente (pra retornar como fonte)
        List<Document> retrieved = vectorStore.similaritySearch(buildSearchRequest(question));

        // 2. Chama o LLM com o advisor (que faz o retrieval de novo, mas tudo bem por ora)
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();

        // 3. Mapeia documentos pra DTOs
        List<SourceDto> sources = retrieved.stream()
                .map(this::toSourceDto)
                .toList();

        return new AskResponse(question, answer, sources);
    }

    private SearchRequest buildSearchRequest() {
        return SearchRequest.builder()
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();
    }

    private SearchRequest buildSearchRequest(String query) {
        return SearchRequest.builder()
                .query(query)
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();
    }

    private SourceDto toSourceDto(Document doc) {
        // Página vem nos metadados, dependendo do reader. Pode ser "page_number" ou similar.
        Integer page = extractPage(doc);

        // Spring AI guarda DISTANCE (0 = idêntico, 1 = oposto).
        // Convertemos para SIMILARITY (1 = idêntico, 0 = sem relação), mais intuitivo.
        Double similarity = Optional.ofNullable(doc.getMetadata().get("distance"))
                .map(d -> ((Number) d).doubleValue())
                .map(distance -> 1.0 - distance)
                .map(s -> Math.round(s * 1000.0) / 1000.0) // arredonda pra 3 casas
                .orElse(null);

        // Trunca o texto pra não poluir a resposta (preview de 300 chars)
        String preview = truncate(doc.getText(), 300);

        return new SourceDto(preview, page, score);
    }

    private Integer extractPage(Document doc) {
        Object pageRaw = doc.getMetadata().get("page_number");
        if (pageRaw instanceof Number n) {
            return n.intValue();
        }
        return null;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}