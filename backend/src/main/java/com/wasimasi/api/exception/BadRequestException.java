package com.wasimasi.api.exception;

/** Se lanza ante una solicitud invalida de negocio (no de validacion de campos). Mapea a HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
