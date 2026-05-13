package com.regulafin.assistant.dto;

public record SourceDto(
        String text,
        Integer page,
        Double score
) {
}