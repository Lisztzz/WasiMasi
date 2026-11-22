package com.wasimasi.api.controller;

import com.wasimasi.api.dto.ContactoRequestDTO;
import com.wasimasi.api.dto.ContactoResponseDTO;
import com.wasimasi.api.dto.ConversacionResponseDTO;
import com.wasimasi.api.dto.DetalleConversacionResponseDTO;
import com.wasimasi.api.dto.LecturaRequestDTO;
import com.wasimasi.api.dto.MensajeRequestDTO;
import com.wasimasi.api.dto.MensajeResponseDTO;
import com.wasimasi.api.dto.MensajesNoLeidosResponseDTO;
import com.wasimasi.api.service.ConversacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** US12/US13/US14 - Primer contacto, chat directo y mensajes sin leer. */
@RestController
@RequiredArgsConstructor
public class ConversacionController {

    private final ConversacionService conversaciones;

    @GetMapping("/publicaciones/{publicacionId}/contacto")
    public ContactoResponseDTO consultarContacto(@AuthenticationPrincipal Jwt jwt, @PathVariable Long publicacionId) {
        return conversaciones.consultarContacto(Long.valueOf(jwt.getSubject()), publicacionId);
    }

    @PostMapping("/publicaciones/{publicacionId}/contacto")
    public ContactoResponseDTO iniciarContacto(@AuthenticationPrincipal Jwt jwt, @PathVariable Long publicacionId,
                                                @Valid @RequestBody ContactoRequestDTO request) {
        return conversaciones.iniciarContacto(Long.valueOf(jwt.getSubject()), publicacionId, request);
    }

    @GetMapping("/conversaciones")
    public List<ConversacionResponseDTO> listar(@AuthenticationPrincipal Jwt jwt) {
        return conversaciones.listar(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/conversaciones/{id}")
    public DetalleConversacionResponseDTO obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return conversaciones.obtener(Long.valueOf(jwt.getSubject()), id);
    }

    @GetMapping("/conversaciones/{id}/mensajes")
    public List<MensajeResponseDTO> listarMensajes(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                   @RequestParam(defaultValue = "0") Long despuesDe) {
        return conversaciones.listarMensajes(Long.valueOf(jwt.getSubject()), id, despuesDe);
    }

    @PostMapping("/conversaciones/{id}/mensajes")
    public MensajeResponseDTO enviar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody MensajeRequestDTO request) {
        return conversaciones.enviar(Long.valueOf(jwt.getSubject()), id, request);
    }

    @PostMapping("/conversaciones/{id}/lectura")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLectura(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                              @Valid @RequestBody LecturaRequestDTO request) {
        conversaciones.marcarLectura(Long.valueOf(jwt.getSubject()), id, request.mensajeId());
    }

    /** US14 - Resumen de mensajes pendientes del usuario autenticado. */
    @GetMapping("/conversaciones/no-leidos")
    public MensajesNoLeidosResponseDTO consultarNoLeidos(@AuthenticationPrincipal Jwt jwt) {
        return conversaciones.consultarNoLeidos(Long.valueOf(jwt.getSubject()));
    }
}
