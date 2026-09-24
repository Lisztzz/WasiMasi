package com.wasimasi.api.exception;

/** Se lanza cuando las credenciales de login son invalidas. Mapea a HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
