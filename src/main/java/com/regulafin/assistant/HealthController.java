package com.regulafin.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final ChatClient chatClient;

    public HealthController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }

    @GetMapping("/chat")
    public Map<String, String> chat(@RequestParam("q") String question) {
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();
        return Map.of("question", question, "answer", answer);
    }
}
