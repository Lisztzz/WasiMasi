package com.wasimasi.api.controller;

import com.wasimasi.api.dto.UsuarioResponseDTO;
import com.wasimasi.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** US03/US04 (verificacion simulada) y US05 (ver estado de verificacion). */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** Datos del usuario autenticado (segun su token). */
    @GetMapping("/me")
    public UsuarioResponseDTO yo(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.obtener(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    /**
     * Simula el resultado de US03 (verificacion de correo institucional)
     * o US04 (verificacion de DNI), segun el rol del usuario. Todavia no
     * hay envio de correo real ni validacion real de documento: este
     * endpoint solo mueve el estado a VERIFICADO para poder seguir
     * probando el resto del flujo (publicar, buscar, etc.).
     * Autorizacion: cada usuario solo puede verificar su propia cuenta (403 si no).
     */
    @PostMapping("/{id}/simular-verificacion")
    public UsuarioResponseDTO simularVerificacion(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.simularVerificacion(id, Long.valueOf(jwt.getSubject()));
    }
}
