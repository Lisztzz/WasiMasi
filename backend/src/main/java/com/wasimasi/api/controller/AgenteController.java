package com.wasimasi.api.controller;

import com.wasimasi.api.dto.AccionAgenteDTO;
import com.wasimasi.api.dto.ConsultaAgenteRequestDTO;
import com.wasimasi.api.dto.RespuestaAgenteDTO;
import com.wasimasi.api.service.AgenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * US21 - Consultar al agente de IA en lenguaje natural.
 * US22 - Confirmar o cancelar la acción que el agente propone.
 */
@RestController
@RequestMapping("/agente")
@RequiredArgsConstructor
public class AgenteController {

    private final AgenteService agenteService;

    @PostMapping("/consultas")
    public RespuestaAgenteDTO consultar(@AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody ConsultaAgenteRequestDTO request) {
        return agenteService.consultar(Long.valueOf(jwt.getSubject()), request);
    }

    @PostMapping("/acciones/{id}/confirmar")
    public AccionAgenteDTO confirmar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return agenteService.confirmar(Long.valueOf(jwt.getSubject()), id);
    }

    @PostMapping("/acciones/{id}/cancelar")
    public AccionAgenteDTO cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return agenteService.cancelar(Long.valueOf(jwt.getSubject()), id);
    }
}
