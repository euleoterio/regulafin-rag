package com.regulafin.assistant.dto;

import java.util.List;

public record AskResponse(
        String question,
        String answer,
        List<SourceDto> sources
) {
}