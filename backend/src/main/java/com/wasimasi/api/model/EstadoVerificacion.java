package com.wasimasi.api.model;

/**
 * Estado de verificacion de un usuario (US03, US04, US05).
 * Por ahora la verificacion es SIMULADA: no hay envio real de correo
 * ni validacion real de DNI, solo el flujo de estados.
 */
public enum EstadoVerificacion {
    PENDIENTE,
    VERIFICADO,
    RECHAZADO
}
