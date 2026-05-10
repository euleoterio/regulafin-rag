package com.regulafin.assistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final ChatClient chatClient;

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
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(4)
                                                .similarityThreshold(0.5)
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}