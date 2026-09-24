package com.wasimasi.api.controller;

import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** US06 - Publicar, US08 - Buscar y US11 - Ver detalle de una publicacion. */
@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    @PostMapping
    public ResponseEntity<PublicacionResponseDTO> publicar(@Valid @RequestBody PublicacionRequestDTO request) {
        PublicacionResponseDTO creada = publicacionService.publicar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    public List<PublicacionResponseDTO> buscar(
            @RequestParam String zona,
            @RequestParam(name = "precioMin") BigDecimal precioMin,
            @RequestParam(name = "precioMax") BigDecimal precioMax) {
        return publicacionService.buscar(zona, precioMin, precioMax);
    }

    @GetMapping("/{id}")
    public PublicacionResponseDTO verDetalle(@PathVariable Long id) {
        return publicacionService.verDetalle(id);
    }
}
