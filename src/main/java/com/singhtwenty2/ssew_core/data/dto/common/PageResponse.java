package com.singhtwenty2.ssew_core.data.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int index;
    private int limit;
    private long total_elements;
    private int total_pages;
    private boolean first;
    private boolean last;
    private boolean empty;
    private int number_of_elements;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .index(page.getNumber())
                .limit(page.getSize())
                .total_elements(page.getTotalElements())
                .total_pages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .number_of_elements(page.getNumberOfElements())
                .build();
    }
}