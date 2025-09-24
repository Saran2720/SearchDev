package com.searchDev.SearchDev.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class PageResponseDTO<T> {
    private List<T> content;
    private int size;
    private int page;
    private long totalElements;
    private int totalPage;
    private boolean last;
}
