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
import com.wasimasi.api.mapper.UsuarioMapperImpl;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.RolUsuario;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.UsuarioRepository;
import com.wasimasi.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de US01 a US05 (registro, login y verificacion simulada).
 * Sin contexto de Spring, sin BD y sin secretos: el JWT es un texto de prueba.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Long USUARIO_ID = 5L;
    private static final String CLAVE_PLANA = "clave123";
    private static final String CLAVE_HASH = "$2a$10$hash-de-prueba";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    /** Mapstruct real, para comprobar lo que recibe el frontend. */
    @Spy
    private UsuarioMapper usuarioMapper = new UsuarioMapperImpl();

    @InjectMocks
    private UsuarioService usuarioService;

    /** El campo @Value es privado: se inyecta igual que haria Spring. */
    @BeforeEach
    void configurarDominiosInstitucionales() {
        ReflectionTestUtils.setField(usuarioService, "dominiosInstitucionalesRaw",
                "upc.edu.pe,pucp.edu.pe,unmsm.edu.pe");
    }

    private static RegistroRequestDTO registroBuscador(String correo) {
        return new RegistroRequestDTO("Ana", correo, CLAVE_PLANA, RolUsuario.BUSCADOR,
                "20261234", "UPC", null);
    }

    private static Usuario usuario(EstadoVerificacion estado) {
        return Usuario.builder()
                .id(USUARIO_ID)
                .nombre("Ana")
                .correo("ana@upc.edu.pe")
                .password(CLAVE_HASH)
                .rol(RolUsuario.BUSCADOR)
                .codigoEstudiante("20261234")
                .universidad("UPC")
                .estadoVerificacion(estado)
                .build();
    }

    private void simularGuardado() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario entidad = invocacion.getArgument(0);
            entidad.setId(USUARIO_ID);
            return entidad;
        });
    }

    // ------------------------------------------------------------------ US01

    @Test
    @DisplayName("US01 - buscador con correo institucional se registra")
    void registrarBuscadorConCorreoInstitucional() {
        when(usuarioRepository.existsByCorreo("ana@upc.edu.pe")).thenReturn(false);
        when(passwordEncoder.encode(CLAVE_PLANA)).thenReturn(CLAVE_HASH);
        simularGuardado();

        UsuarioResponseDTO respuesta = usuarioService.registrar(registroBuscador("ana@upc.edu.pe"));

        assertThat(respuesta.getId()).isEqualTo(USUARIO_ID);
        assertThat(respuesta.getNombre()).isEqualTo("Ana");
        assertThat(respuesta.getCorreo()).isEqualTo("ana@upc.edu.pe");
        assertThat(respuesta.getRol()).isEqualTo(RolUsuario.BUSCADOR);
        assertThat(respuesta.getUniversidad()).isEqualTo("UPC");
        assertThat(respuesta.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.PENDIENTE);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getCodigoEstudiante()).isEqualTo("20261234");
        assertThat(guardado.getPassword()).isEqualTo(CLAVE_HASH);
        assertThat(guardado.getPassword()).isNotEqualTo(CLAVE_PLANA);
        verify(passwordEncoder).encode(CLAVE_PLANA);
    }

    @Test
    @DisplayName("US01 - el dominio institucional se acepta aunque venga en mayusculas")
    void registrarBuscadorConCorreoInstitucionalEnMayusculas() {
        when(usuarioRepository.existsByCorreo("ANA@UPC.EDU.PE")).thenReturn(false);
        when(passwordEncoder.encode(CLAVE_PLANA)).thenReturn(CLAVE_HASH);
        simularGuardado();

        UsuarioResponseDTO respuesta = usuarioService.registrar(registroBuscador("ANA@UPC.EDU.PE"));

        assertThat(respuesta.getCorreo()).isEqualTo("ANA@UPC.EDU.PE");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("US01 - buscador con correo no institucional es rechazado")
    void registrarBuscadorConCorreoNoInstitucionalLanzaBadRequest() {
        when(usuarioRepository.existsByCorreo("ana@gmail.com")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.registrar(registroBuscador("ana@gmail.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usa tu correo institucional para registrarte como buscador");

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("US01 - anfitrion se registra con DNI aunque el correo no sea institucional")
    void registrarAnfitrionConDni() {
        RegistroRequestDTO request = new RegistroRequestDTO("Carlos", "carlos@gmail.com", CLAVE_PLANA,
                RolUsuario.ANFITRION, null, null, "12345678");
        when(usuarioRepository.existsByCorreo("carlos@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(CLAVE_PLANA)).thenReturn(CLAVE_HASH);
        simularGuardado();

        UsuarioResponseDTO respuesta = usuarioService.registrar(request);

        assertThat(respuesta.getRol()).isEqualTo(RolUsuario.ANFITRION);
        assertThat(respuesta.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.PENDIENTE);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getDocumentoIdentidad()).isEqualTo("12345678");
        assertThat(captor.getValue().getCodigoEstudiante()).isNull();
        assertThat(captor.getValue().getPassword()).isEqualTo(CLAVE_HASH);
    }

    @Test
    @DisplayName("US01 - correo duplicado es conflicto")
    void registrarCorreoDuplicadoLanzaConflict() {
        when(usuarioRepository.existsByCorreo("ana@upc.edu.pe")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(registroBuscador("ana@upc.edu.pe")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ya existe una cuenta con este correo");

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ US02

    @Test
    @DisplayName("US02 - login correcto con verificacion pendiente")
    void loginCorrectoRequiereVerificacion() {
        Usuario usuario = usuario(EstadoVerificacion.PENDIENTE);
        when(usuarioRepository.findByCorreo("ana@upc.edu.pe")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(CLAVE_PLANA, CLAVE_HASH)).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("token-de-prueba");
        when(jwtService.getExpiracionSegundos()).thenReturn(7200L);

        LoginResponseDTO respuesta = usuarioService.login(new LoginRequestDTO("ana@upc.edu.pe", CLAVE_PLANA));

        assertThat(respuesta.getToken()).isEqualTo("token-de-prueba");
        assertThat(respuesta.getTipoToken()).isEqualTo("Bearer");
        assertThat(respuesta.getExpiraEnSegundos()).isEqualTo(7200L);
        assertThat(respuesta.isRequiereVerificacion()).isTrue();
        assertThat(respuesta.getUsuario().getCorreo()).isEqualTo("ana@upc.edu.pe");
        assertThat(respuesta.getUsuario().getRol()).isEqualTo(RolUsuario.BUSCADOR);
        verify(passwordEncoder).matches(CLAVE_PLANA, CLAVE_HASH);
    }

    @Test
    @DisplayName("US02 - login de usuario verificado no pide verificacion")
    void loginDeUsuarioVerificadoNoRequiereVerificacion() {
        Usuario usuario = usuario(EstadoVerificacion.VERIFICADO);
        when(usuarioRepository.findByCorreo("ana@upc.edu.pe")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(CLAVE_PLANA, CLAVE_HASH)).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("token-de-prueba");
        when(jwtService.getExpiracionSegundos()).thenReturn(7200L);

        LoginResponseDTO respuesta = usuarioService.login(new LoginRequestDTO("ana@upc.edu.pe", CLAVE_PLANA));

        assertThat(respuesta.isRequiereVerificacion()).isFalse();
        assertThat(respuesta.getUsuario().getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADO);
    }

    @Test
    @DisplayName("US02 - contrasena incorrecta: no se genera token")
    void loginConContrasenaIncorrectaLanzaUnauthorized() {
        when(usuarioRepository.findByCorreo("ana@upc.edu.pe"))
                .thenReturn(Optional.of(usuario(EstadoVerificacion.PENDIENTE)));
        when(passwordEncoder.matches(CLAVE_PLANA, CLAVE_HASH)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(new LoginRequestDTO("ana@upc.edu.pe", CLAVE_PLANA)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Correo o contrasena incorrectos");

        verify(jwtService, never()).generarToken(any());
    }

    @Test
    @DisplayName("US02 - correo inexistente: no se genera token")
    void loginConCorreoInexistenteLanzaUnauthorized() {
        when(usuarioRepository.findByCorreo("nadie@upc.edu.pe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(new LoginRequestDTO("nadie@upc.edu.pe", CLAVE_PLANA)))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generarToken(any());
    }

    // -------------------------------------------------------------- US03-US05

    @Test
    @DisplayName("US03/US04 - simular verificacion del propio usuario")
    void simularVerificacionDelPropioUsuario() {
        Usuario usuario = usuario(EstadoVerificacion.PENDIENTE);
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO respuesta = usuarioService.simularVerificacion(USUARIO_ID, USUARIO_ID);

        assertThat(respuesta.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADO);
        assertThat(usuario.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADO);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("US03/US04 - no se puede verificar la cuenta de otro")
    void simularVerificacionDeOtroUsuarioLanzaForbidden() {
        assertThatThrownBy(() -> usuarioService.simularVerificacion(USUARIO_ID, 999L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Solo puedes verificar tu propia cuenta");

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("US03/US04 - usuario inexistente al simular verificacion")
    void simularVerificacionDeUsuarioInexistenteLanzaResourceNotFound() {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.simularVerificacion(USUARIO_ID, USUARIO_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontro el usuario con id " + USUARIO_ID);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("US05 - obtener el perfil del usuario autenticado")
    void obtenerUsuarioExistente() {
        when(usuarioRepository.findById(USUARIO_ID))
                .thenReturn(Optional.of(usuario(EstadoVerificacion.VERIFICADO)));

        UsuarioResponseDTO respuesta = usuarioService.obtener(USUARIO_ID);

        assertThat(respuesta.getId()).isEqualTo(USUARIO_ID);
        assertThat(respuesta.getNombre()).isEqualTo("Ana");
        assertThat(respuesta.getCorreo()).isEqualTo("ana@upc.edu.pe");
        assertThat(respuesta.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADO);
    }

    @Test
    @DisplayName("US05 - usuario inexistente al obtener el perfil")
    void obtenerUsuarioInexistenteLanzaResourceNotFound() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtener(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontro el usuario con id 404");
    }
}
