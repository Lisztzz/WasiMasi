package com.wasimasi.api.controller;

import com.wasimasi.api.dto.LoginRequestDTO;
import com.wasimasi.api.dto.LoginResponseDTO;
import com.wasimasi.api.dto.RegistroRequestDTO;
import com.wasimasi.api.dto.UsuarioResponseDTO;
import com.wasimasi.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US01 - Registro y US02 - Inicio de sesion. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        UsuarioResponseDTO creado = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }
}
