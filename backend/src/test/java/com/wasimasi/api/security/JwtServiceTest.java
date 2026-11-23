package com.wasimasi.api.security;

import com.wasimasi.api.model.RolUsuario;
import com.wasimasi.api.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del JWT del login (US02). El codificador es un mock: no se
 * usa ninguna clave real ni se firma nada de verdad.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtService jwtService;

    /** El campo @Value es privado: se inyecta el valor de prueba por reflexion. */
    @BeforeEach
    void configurarExpiracion() {
        ReflectionTestUtils.setField(jwtService, "expiracionMinutos", 30L);
    }

    @Test
    @DisplayName("US02 - el token lleva subject, correo, roles, emisor y expiracion")
    void generarTokenIncluyeSubjectClaimsYExpiracion() {
        Usuario usuario = Usuario.builder()
                .id(7L)
                .correo("ana@upc.edu.pe")
                .rol(RolUsuario.BUSCADOR)
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocacion -> {
            JwtEncoderParameters parametros = invocacion.getArgument(0);
            JwtClaimsSet claims = parametros.getClaims();
            return new Jwt("token-de-prueba", claims.getIssuedAt(), claims.getExpiresAt(),
                    Map.of("alg", "HS256"), claims.getClaims());
        });

        String token = jwtService.generarToken(usuario);

        assertThat(token).isEqualTo("token-de-prueba");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters parametros = captor.getValue();
        JwtClaimsSet claims = parametros.getClaims();
        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.getClaims())
                .containsEntry("iss", "wasimasi-api")
                .containsEntry("correo", "ana@upc.edu.pe")
                .containsEntry("roles", List.of("BUSCADOR"));
        assertThat(claims.getIssuedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
        assertThat(claims.getExpiresAt()).isEqualTo(claims.getIssuedAt().plus(30, ChronoUnit.MINUTES));
        assertThat(parametros.getJwsHeader().getAlgorithm()).isEqualTo(MacAlgorithm.HS256);
    }

    @Test
    @DisplayName("US02 - la expiracion informada al frontend esta en segundos")
    void getExpiracionSegundosConvierteMinutos() {
        assertThat(jwtService.getExpiracionSegundos()).isEqualTo(1800L);
    }
}
