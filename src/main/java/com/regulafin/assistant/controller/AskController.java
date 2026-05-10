package com.regulafin.assistant.controller;

import com.regulafin.assistant.service.RagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AskController {

    private final RagService ragService;

    public AskController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/ask")
    public Map<String, String> ask(@RequestParam("q") String question) {
        String answer = ragService.ask(question);
        return Map.of("question", question, "answer", answer);
    }
}
