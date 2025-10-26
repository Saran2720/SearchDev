package com.searchDev.SearchDev.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResDTO<T> {
    private T data;
    private int status;
    private String message;
    private boolean success;
    private LocalDateTime timestamp;
}
