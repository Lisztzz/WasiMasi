package com.wasimasi.api.exception;

/** 503 - Un servicio externo (por ejemplo, el modelo de IA) no está disponible o no está configurado. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String message) {
        super(message);
    }
}
