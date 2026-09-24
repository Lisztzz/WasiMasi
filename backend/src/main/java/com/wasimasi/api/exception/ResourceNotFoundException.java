package com.wasimasi.api.exception;

/** Se lanza cuando no se encuentra un recurso (usuario, publicacion, etc.). Mapea a HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
