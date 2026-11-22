package com.wasimasi.api.controller;

import com.wasimasi.api.dto.VinculoViviendaRequestDTO;
import com.wasimasi.api.dto.ViviendaRequestDTO;
import com.wasimasi.api.dto.ViviendaResponseDTO;
import com.wasimasi.api.service.ViviendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** US15 - Gestión de las viviendas del anfitrión. */
@RestController
@RequiredArgsConstructor
public class ViviendaController {
    private final ViviendaService viviendas;

    @GetMapping("/viviendas")
    public List<ViviendaResponseDTO> listar(@AuthenticationPrincipal Jwt jwt) {
        return viviendas.listar(Long.valueOf(jwt.getSubject()));
    }

    @PostMapping("/viviendas")
    @ResponseStatus(HttpStatus.CREATED)
    public ViviendaResponseDTO crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ViviendaRequestDTO request) {
        return viviendas.crear(Long.valueOf(jwt.getSubject()), request);
    }

    @PutMapping("/publicaciones/{id}/vivienda")
    public ViviendaResponseDTO vincular(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                        @Valid @RequestBody VinculoViviendaRequestDTO request) {
        return viviendas.vincular(Long.valueOf(jwt.getSubject()), id, request.viviendaId());
    }
}
