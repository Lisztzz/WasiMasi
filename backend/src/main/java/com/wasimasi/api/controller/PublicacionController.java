package com.wasimasi.api.controller;

import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.dto.BusquedaRequestDTO;
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
 * US06 - Publicar, US08 - Buscar, US11 - Ver detalle y US20 - registrar filtros anónimos.
 * Todas requieren "Authorization: Bearer <token>".
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

    @GetMapping
    public List<PublicacionResponseDTO> buscar(
            @RequestParam String zona,
            @RequestParam(name = "precioMin") BigDecimal precioMin,
            @RequestParam(name = "precioMax") BigDecimal precioMax) {
        return publicacionService.buscar(zona, precioMin, precioMax);
    }

    /** US08/US20 - Buscar habitaciones y registrar únicamente los filtros agregados para reportes. */
    @PostMapping("/buscar")
    public List<PublicacionResponseDTO> buscarYRegistrar(@Valid @RequestBody BusquedaRequestDTO request) {
        return publicacionService.buscarYRegistrar(request);
    }

    @GetMapping("/{id}")
    public PublicacionResponseDTO verDetalle(@PathVariable Long id) {
        return publicacionService.verDetalle(id);
    }
}
