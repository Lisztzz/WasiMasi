package com.wasimasi.api.controller;

import com.wasimasi.api.dto.DetalleGrupoResponseDTO;
import com.wasimasi.api.dto.GrupoResponseDTO;
import com.wasimasi.api.dto.LecturaRequestDTO;
import com.wasimasi.api.dto.MensajeGrupoResponseDTO;
import com.wasimasi.api.dto.MensajeRequestDTO;
import com.wasimasi.api.service.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** US15 - Chats grupales de las viviendas a las que pertenece la cuenta. */
@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GrupoController {
    private final GrupoService grupos;

    @GetMapping
    public List<GrupoResponseDTO> listar(@AuthenticationPrincipal Jwt jwt) {
        return grupos.listar(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public DetalleGrupoResponseDTO obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return grupos.obtener(Long.valueOf(jwt.getSubject()), id);
    }

    @GetMapping("/{id}/mensajes")
    public DetalleGrupoResponseDTO listarMensajes(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                    @RequestParam(defaultValue = "0") Long despuesDe) {
        return grupos.listarMensajes(Long.valueOf(jwt.getSubject()), id, despuesDe);
    }

    @PostMapping("/{id}/mensajes")
    public MensajeGrupoResponseDTO enviar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                         @Valid @RequestBody MensajeRequestDTO request) {
        return grupos.enviar(Long.valueOf(jwt.getSubject()), id, request);
    }

    @PostMapping("/{id}/lectura")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLectura(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                              @Valid @RequestBody LecturaRequestDTO request) {
        grupos.marcarLectura(Long.valueOf(jwt.getSubject()), id, request.mensajeId());
    }
}
