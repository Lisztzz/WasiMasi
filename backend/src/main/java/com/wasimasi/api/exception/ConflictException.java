package com.wasimasi.api.exception;

/** Se lanza ante un conflicto de negocio (ej. correo ya registrado). Mapea a HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
