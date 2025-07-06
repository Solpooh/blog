package com.solpooh.boardback.dto.response.board;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponseDto {
    private String name;
    private long count;
}
