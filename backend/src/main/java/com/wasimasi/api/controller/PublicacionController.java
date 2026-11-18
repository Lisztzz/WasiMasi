package com.wasimasi.api.controller;

import com.wasimasi.api.dto.EditarPublicacionRequestDTO;
import com.wasimasi.api.dto.EstadoPublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.exception.UnauthorizedException;
import com.wasimasi.api.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * US06 - Publicar (solo rol ANFITRION), US08 - Buscar y US11 - Ver detalle
 * (cualquier usuario autenticado). Todas requieren "Authorization: Bearer <token>".
 */
@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    @PostMapping
    public ResponseEntity<PublicacionResponseDTO> publicar(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody PublicacionRequestDTO request) {
        PublicacionResponseDTO creada = publicacionService.publicar(obtenerAnfitrionId(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** US07 - Listar las publicaciones del anfitrión autenticado. */
    @GetMapping("/mias")
    public List<PublicacionResponseDTO> misPublicaciones(@AuthenticationPrincipal Jwt jwt) {
        return publicacionService.misPublicaciones(obtenerAnfitrionId(jwt));
    }

    /** US07 - Editar los datos de una publicación propia. */
    @PutMapping("/{id}")
    public PublicacionResponseDTO editar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                         @Valid @RequestBody EditarPublicacionRequestDTO request) {
        return publicacionService.editar(obtenerAnfitrionId(jwt), id, request);
    }

    /** US07 y US18 - Cambiar el estado de una publicación propia. */
    @PatchMapping("/{id}/estado")
    public PublicacionResponseDTO cambiarEstado(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                @Valid @RequestBody EstadoPublicacionRequestDTO request) {
        return publicacionService.cambiarEstado(obtenerAnfitrionId(jwt), id, request);
    }

    private long obtenerAnfitrionId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new UnauthorizedException("El token no contiene un usuario valido");
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("El token contiene un usuario invalido");
        }
    }

    @GetMapping
    public List<PublicacionResponseDTO> buscar(
            @RequestParam(required = false) String zona,
            @RequestParam(name = "precioMin", required = false) BigDecimal precioMin,
            @RequestParam(name = "precioMax", required = false) BigDecimal precioMax) {
        return publicacionService.buscar(zona, precioMin, precioMax);
    }

    @GetMapping("/{id}")
    public PublicacionResponseDTO verDetalle(@PathVariable Long id) {
        return publicacionService.verDetalle(id);
    }
}
