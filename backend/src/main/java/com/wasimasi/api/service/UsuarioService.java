package com.wasimasi.api.service;

import com.wasimasi.api.dto.LoginRequestDTO;
import com.wasimasi.api.dto.LoginResponseDTO;
import com.wasimasi.api.dto.RegistroRequestDTO;
import com.wasimasi.api.dto.UsuarioResponseDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ConflictException;
import com.wasimasi.api.exception.ForbiddenException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.exception.UnauthorizedException;
import com.wasimasi.api.mapper.UsuarioMapper;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.RolUsuario;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.UsuarioRepository;
import com.wasimasi.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Reglas de negocio de US01 (registro), US02 (login) y US03/US04/US05
 * (verificacion). La verificacion esta SIMULADA por ahora: no se envia
 * correo real ni se valida el DNI contra un servicio externo; solo se
 * modela el flujo de estados (PENDIENTE -> VERIFICADO / RECHAZADO).
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${wasimasi.dominios-institucionales}")
    private String dominiosInstitucionalesRaw;

    public UsuarioResponseDTO registrar(RegistroRequestDTO request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new ConflictException("Ya existe una cuenta con este correo");
        }

        if (request.getRol() == RolUsuario.BUSCADOR && !tieneCorreoInstitucional(request.getCorreo())) {
            throw new BadRequestException("Usa tu correo institucional para registrarte como buscador");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                // La contrasena se guarda hasheada con BCrypt, nunca en texto plano.
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .codigoEstudiante(request.getCodigoEstudiante())
                .universidad(request.getUniversidad())
                .documentoIdentidad(request.getDocumentoIdentidad())
                .estadoVerificacion(EstadoVerificacion.PENDIENTE)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(guardado);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Correo o contrasena incorrectos"));

        boolean requiereVerificacion = usuario.getEstadoVerificacion() == EstadoVerificacion.PENDIENTE;

        return LoginResponseDTO.builder()
                .token(jwtService.generarToken(usuario))
                .tipoToken("Bearer")
                .expiraEnSegundos(jwtService.getExpiracionSegundos())
                .usuario(usuarioMapper.toResponseDTO(usuario))
                .requiereVerificacion(requiereVerificacion)
                .build();
    }

    /**
     * US03/US04 SIMULADAS: en la version real esto lo dispara la
     * confirmacion del correo de verificacion o la revision del DNI por
     * el equipo de moderacion. Por ahora, este endpoint simula ese
     * resultado directamente para poder avanzar con el resto del flujo.
     */
    public UsuarioResponseDTO simularVerificacion(Long usuarioId, Long usuarioAutenticadoId) {
        if (!usuarioId.equals(usuarioAutenticadoId)) {
            throw new ForbiddenException("Solo puedes verificar tu propia cuenta");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario con id " + usuarioId));

        usuario.setEstadoVerificacion(EstadoVerificacion.VERIFICADO);
        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(guardado);
    }

    public UsuarioResponseDTO obtener(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario con id " + usuarioId));
        return usuarioMapper.toResponseDTO(usuario);
    }

    private boolean tieneCorreoInstitucional(String correo) {
        List<String> dominios = Arrays.asList(dominiosInstitucionalesRaw.split(","));
        return dominios.stream()
                .anyMatch(dominio -> correo.toLowerCase().endsWith("@" + dominio.trim().toLowerCase()));
    }
}
