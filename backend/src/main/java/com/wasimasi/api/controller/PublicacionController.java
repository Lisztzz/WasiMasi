package com.wasimasi.api.controller;

import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
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
 * US06 - Publicar (solo rol ANFITRION), US08/US09 - Buscar y US11 - Ver detalle
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
        Long anfitrionId = Long.valueOf(jwt.getSubject());
        PublicacionResponseDTO creada = publicacionService.publicar(anfitrionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** US08 + US09 - Los filtros de condiciones son opcionales (compatibles con el buscador anterior). */
    @GetMapping
    public List<PublicacionResponseDTO> buscar(
            @RequestParam String zona,
            @RequestParam(name = "precioMin") BigDecimal precioMin,
            @RequestParam(name = "precioMax") BigDecimal precioMax,
            @RequestParam(name = "mascotas", required = false) Boolean mascotas,
            @RequestParam(name = "genero", required = false) String genero,
            @RequestParam(name = "servicios", required = false) String servicios) {
        return publicacionService.buscar(zona, precioMin, precioMax, mascotas, genero, servicios);
    }

    @GetMapping("/{id}")
    public PublicacionResponseDTO verDetalle(@PathVariable Long id) {
        return publicacionService.verDetalle(id);
    }
}