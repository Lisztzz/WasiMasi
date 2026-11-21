package com.wasimasi.api.controller;

import com.wasimasi.api.dto.InteresadoComparacionDTO;
import com.wasimasi.api.dto.VisitaRequestDTO;
import com.wasimasi.api.dto.VisitaResponseDTO;
import com.wasimasi.api.dto.VisitaRespuestaRequestDTO;
import com.wasimasi.api.service.VisitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** US16/US17/US19 - Solicitudes de visita y gestión de interesados. */
@RestController
@RequestMapping("/visitas")
@RequiredArgsConstructor
public class VisitaController {

    private final VisitaService visitaService;

    /** US16 - Solicitar una visita con la identidad obtenida del JWT. */
    @PostMapping
    public ResponseEntity<VisitaResponseDTO> solicitar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VisitaRequestDTO request) {
        VisitaResponseDTO creada = visitaService.solicitar(Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** US17 - Consultar las solicitudes recibidas por el anfitrión autenticado. */
    @GetMapping("/mis-visitas")
    public List<VisitaResponseDTO> misVisitas(@AuthenticationPrincipal Jwt jwt) {
        return visitaService.misVisitasComoAnfitrion(Long.valueOf(jwt.getSubject()));
    }

    /** US17 - Confirmar o rechazar una solicitud de una publicación propia. */
    @PatchMapping("/{id}/respuesta")
    public VisitaResponseDTO responder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody VisitaRespuestaRequestDTO request) {
        return visitaService.responder(Long.valueOf(jwt.getSubject()), id, request);
    }

    /** US19 - Comparar a quienes solicitaron una visita para una publicación propia. */
    @GetMapping("/publicacion/{publicacionId}/interesados")
    public List<InteresadoComparacionDTO> interesados(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long publicacionId) {
        return visitaService.compararInteresados(Long.valueOf(jwt.getSubject()), publicacionId);
    }
}
