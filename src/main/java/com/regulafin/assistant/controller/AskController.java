package com.regulafin.assistant.controller;

import com.regulafin.assistant.dto.AskResponse;
import com.regulafin.assistant.service.RagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {

    private final RagService ragService;

    public AskController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/ask")
    public AskResponse ask(@RequestParam("q") String question) {
        return ragService.ask(question);
    }
}