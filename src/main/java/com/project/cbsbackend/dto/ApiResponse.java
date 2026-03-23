package com.project.cbsbackend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private String status;   // success / error
    private String message;
    private T data;
}