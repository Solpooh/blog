package com.solpooh.boardback.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Pagination<T> {
    private List<T> content;
    private Integer page;  // 현재 페이지
    private Integer size;  // 한 페이지에 보여줄 elements
    private Integer totalPages;  // 총 페이지 수
    private Long totalElements;  // 총 elements 수
    private Boolean first;  // 첫 페이지 여부
    private Boolean last;  // 마지막 페이지 여부
    private Integer numberOfElements;  // 현재 페이지에 반환된 elements
    public static <T> Pagination<T> of(Page<?> page, List<T> content) {
        return new Pagination<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements()
        );
    }
}
