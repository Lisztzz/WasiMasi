package com.wasimasi.api.service;

import com.wasimasi.api.dto.AccionAgenteDTO;
import com.wasimasi.api.dto.ConsultaAgenteRequestDTO;
import com.wasimasi.api.dto.VisitaRequestDTO;
import com.wasimasi.api.dto.VisitaResponseDTO;
import com.wasimasi.api.exception.ConflictException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.exception.ServicioNoDisponibleException;
import com.wasimasi.api.model.AccionAgente;
import com.wasimasi.api.model.EstadoAccionAgente;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.TipoAccionAgente;
import com.wasimasi.api.repository.AccionAgenteRepository;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de US21/US22 (agente de IA). El modelo no se llama: se prueba que
 * sin clave no se consulta y que las acciones solo se ejecutan al confirmar.
 */
@ExtendWith(MockitoExtension.class)
class AgenteServiceTest {

    private static final Long BUSCADOR_ID = 7L;
    private static final Long ACCION_ID = 30L;
    private static final Instant AHORA = Instant.parse("2026-11-22T15:00:00Z");

    @Mock private ChatClient chatClient;
    @Mock private PublicacionService publicacionService;
    @Mock private VisitaService visitaService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PublicacionRepository publicacionRepository;
    @Mock private AccionAgenteRepository accionRepository;

    private AgenteService servicio;

    @BeforeEach
    void setUp() {
        servicio = crear("gsk_clave_de_prueba");
    }

    private AgenteService crear(String apiKey) {
        return new AgenteService(chatClient, publicacionService, visitaService, usuarioRepository,
                publicacionRepository, accionRepository, apiKey, Clock.fixed(AHORA, AgenteService.ZONA_LIMA));
    }

    private AccionAgente accion(EstadoAccionAgente estado, LocalDateTime creadaEn) {
        return AccionAgente.builder()
                .id(ACCION_ID)
                .tipo(TipoAccionAgente.SOLICITAR_VISITA)
                .estado(estado)
                .publicacionId(3L)
                .fechaHora(LocalDateTime.now(Clock.fixed(AHORA, AgenteService.ZONA_LIMA)).plusDays(2))
                .comentario("Voy en la tarde")
                .creadaEn(creadaEn)
                .build();
    }

    private LocalDateTime haceMinutos(long minutos) {
        return LocalDateTime.now(Clock.fixed(AHORA, AgenteService.ZONA_LIMA)).minusMinutes(minutos);
    }

    @Test
    @DisplayName("US21 - sin GROQ_API_KEY responde 503 y no llama al modelo")
    void consultarSinClaveLanzaServicioNoDisponible() {
        AgenteService sinClave = crear(AgenteService.CLAVE_SIN_CONFIGURAR);

        assertThatThrownBy(() -> sinClave.consultar(BUSCADOR_ID, new ConsultaAgenteRequestDTO("hola", null)))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("GROQ_API_KEY");
        verifyNoInteractions(chatClient, usuarioRepository);
    }

    @Test
    @DisplayName("US22 - confirmar crea la visita con los datos propuestos y marca la acción CONFIRMADA")
    void confirmarCreaLaVisita() {
        AccionAgente pendiente = accion(EstadoAccionAgente.PENDIENTE, haceMinutos(5));
        when(accionRepository.findByIdAndUsuarioId(ACCION_ID, BUSCADOR_ID)).thenReturn(Optional.of(pendiente));
        VisitaResponseDTO visita = VisitaResponseDTO.builder().id(99L).build();
        when(visitaService.solicitar(eq(BUSCADOR_ID), any())).thenReturn(visita);
        Publicacion publicacion = Publicacion.builder().id(3L).zona("Barranco").build();
        when(publicacionRepository.findById(3L)).thenReturn(Optional.of(publicacion));

        AccionAgenteDTO resultado = servicio.confirmar(BUSCADOR_ID, ACCION_ID);

        ArgumentCaptor<VisitaRequestDTO> solicitud = ArgumentCaptor.forClass(VisitaRequestDTO.class);
        verify(visitaService).solicitar(eq(BUSCADOR_ID), solicitud.capture());
        assertThat(solicitud.getValue().getPublicacionId()).isEqualTo(3L);
        assertThat(solicitud.getValue().getComentario()).isEqualTo("Voy en la tarde");
        assertThat(resultado.estado()).isEqualTo(EstadoAccionAgente.CONFIRMADA);
        assertThat(resultado.visitaId()).isEqualTo(99L);
        assertThat(resultado.zona()).isEqualTo("Barranco");
    }

    @Test
    @DisplayName("US22 - cancelar no crea ninguna visita")
    void cancelarNoCreaVisita() {
        AccionAgente pendiente = accion(EstadoAccionAgente.PENDIENTE, haceMinutos(1));
        when(accionRepository.findByIdAndUsuarioId(ACCION_ID, BUSCADOR_ID)).thenReturn(Optional.of(pendiente));

        AccionAgenteDTO resultado = servicio.cancelar(BUSCADOR_ID, ACCION_ID);

        assertThat(resultado.estado()).isEqualTo(EstadoAccionAgente.CANCELADA);
        verifyNoInteractions(visitaService);
    }

    @Test
    @DisplayName("US22 - una acción ya resuelta no se ejecuta dos veces")
    void confirmarAccionResueltaLanzaConflict() {
        when(accionRepository.findByIdAndUsuarioId(ACCION_ID, BUSCADOR_ID))
                .thenReturn(Optional.of(accion(EstadoAccionAgente.CONFIRMADA, haceMinutos(1))));

        assertThatThrownBy(() -> servicio.confirmar(BUSCADOR_ID, ACCION_ID)).isInstanceOf(ConflictException.class);
        verifyNoInteractions(visitaService);
    }

    @Test
    @DisplayName("US22 - una propuesta con más de 30 minutos vence y no se ejecuta")
    void confirmarPropuestaVencidaLaMarcaExpirada() {
        AccionAgente vieja = accion(EstadoAccionAgente.PENDIENTE, haceMinutos(31));
        when(accionRepository.findByIdAndUsuarioId(ACCION_ID, BUSCADOR_ID)).thenReturn(Optional.of(vieja));

        assertThatThrownBy(() -> servicio.confirmar(BUSCADOR_ID, ACCION_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("venció");
        assertThat(vieja.getEstado()).isEqualTo(EstadoAccionAgente.EXPIRADA);
        verify(visitaService, never()).solicitar(any(), any());
    }

    @Test
    @DisplayName("US22 - solo quien recibió la propuesta puede resolverla")
    void confirmarAccionDeOtroUsuarioLanzaNotFound() {
        when(accionRepository.findByIdAndUsuarioId(ACCION_ID, 8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.confirmar(8L, ACCION_ID)).isInstanceOf(ResourceNotFoundException.class);
    }
}
