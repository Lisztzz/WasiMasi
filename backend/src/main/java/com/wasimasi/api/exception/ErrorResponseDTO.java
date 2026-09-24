package com.wasimasi.api.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Formato estandar de error de la API: {timestamp, status, error, message}. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
