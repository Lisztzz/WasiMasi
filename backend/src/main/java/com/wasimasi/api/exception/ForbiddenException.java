package com.wasimasi.api.exception;

/** Se lanza cuando un usuario autenticado intenta actuar sobre un recurso ajeno. Mapea a HTTP 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
