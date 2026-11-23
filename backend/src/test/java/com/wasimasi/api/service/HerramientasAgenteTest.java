package com.wasimasi.api.service;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.model.AccionAgente;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.RolUsuario;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.AccionAgenteRepository;
import com.wasimasi.api.repository.PublicacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Tests de las herramientas que el modelo de IA puede llamar (US21/US22). */
@ExtendWith(MockitoExtension.class)
class HerramientasAgenteTest {

    @Mock private PublicacionService publicacionService;
    @Mock private PublicacionRepository publicacionRepository;
    @Mock private AccionAgenteRepository accionRepository;

    private HerramientasAgente herramientas(RolUsuario rol) {
        Usuario usuario = Usuario.builder().id(7L).nombre("Sofia").rol(rol).build();
        return new HerramientasAgente(usuario, publicacionService, publicacionRepository, accionRepository);
    }

    private static PublicacionResponseDTO publicacion(long id) {
        PublicacionResponseDTO p = new PublicacionResponseDTO();
        p.setId(id);
        p.setZona("Santiago de Surco");
        p.setPrecio(new BigDecimal("650"));
        p.setDescripcion("Habitacion " + id);
        p.setEstado(EstadoPublicacion.ACTIVA);
        return p;
    }

    @Test
    @DisplayName("US21 - normaliza la zona igual que la Web App y entiende nombres cortos")
    void normalizaZonas() {
        assertThat(HerramientasAgente.normalizarZona("  Jesús   María ")).isEqualTo("Jesus Maria");
        assertThat(HerramientasAgente.normalizarZona("surco")).isEqualTo("Santiago de Surco");
        assertThat(HerramientasAgente.normalizarZona("SJL")).isEqualTo("San Juan de Lurigancho");
        assertThat(HerramientasAgente.normalizarZona("Miraflores")).isEqualTo("Miraflores");
    }

    @Test
    @DisplayName("US21 - busca con los filtros pedidos y guarda solo las primeras habitaciones reales")
    void buscarGuardaLasHabitacionesEncontradas() {
        List<PublicacionResponseDTO> diez = LongStream.rangeClosed(1, 10).mapToObj(HerramientasAgenteTest::publicacion).toList();
        when(publicacionService.buscar(eq("Santiago de Surco"), any(), any(), eq(true), eq(null), eq("wifi")))
                .thenReturn(diez);
        HerramientasAgente h = herramientas(RolUsuario.BUSCADOR);

        List<Map<String, Object>> resumen = h.buscarHabitaciones("Surco", BigDecimal.ZERO, new BigDecimal("900"), true, null, "wifi");

        assertThat(resumen).hasSize(HerramientasAgente.MAX_RESULTADOS);
        assertThat(h.publicacionesEncontradas()).extracting(PublicacionResponseDTO::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
    }

    @Test
    @DisplayName("US22 - proponer una visita la deja PENDIENTE sin crearla")
    void proponerVisitaGuardaAccionPendiente() {
        Publicacion activa = Publicacion.builder().id(3L).estado(EstadoPublicacion.ACTIVA).build();
        when(publicacionRepository.findById(3L)).thenReturn(Optional.of(activa));
        when(accionRepository.save(any(AccionAgente.class))).thenAnswer(inv -> {
            AccionAgente a = inv.getArgument(0);
            a.setId(30L);
            return a;
        });
        HerramientasAgente h = herramientas(RolUsuario.BUSCADOR);
        String manana = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).toString().substring(0, 16);

        String respuesta = h.proponerVisita(3L, manana, "  Voy con mi mama  ");

        assertThat(respuesta).contains("NO esta agendada");
        assertThat(h.accionPropuesta().getComentario()).isEqualTo("Voy con mi mama");
        assertThat(h.accionPropuesta().getPublicacionId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("US22 - un anfitrión no puede recibir propuestas de visita")
    void anfitrionNoPuedeProponerVisita() {
        HerramientasAgente h = herramientas(RolUsuario.ANFITRION);

        String respuesta = h.proponerVisita(3L, "2030-01-01T10:00", null);

        assertThat(respuesta).startsWith("No se puede proponer");
        assertThat(h.accionPropuesta()).isNull();
        verifyNoInteractions(accionRepository);
    }

    @Test
    @DisplayName("US22 - rechaza fechas pasadas, mal escritas y habitaciones no disponibles")
    void rechazaPropuestasInvalidas() {
        Publicacion pausada = Publicacion.builder().id(4L).estado(EstadoPublicacion.PAUSADA).build();
        Publicacion activa = Publicacion.builder().id(3L).estado(EstadoPublicacion.ACTIVA).build();
        when(publicacionRepository.findById(4L)).thenReturn(Optional.of(pausada));
        when(publicacionRepository.findById(3L)).thenReturn(Optional.of(activa));
        HerramientasAgente h = herramientas(RolUsuario.BUSCADOR);

        assertThat(h.proponerVisita(4L, "2030-01-01T10:00", null)).contains("ya no esta disponible");
        assertThat(h.proponerVisita(3L, "2020-01-01T10:00", null)).contains("debe ser futura");
        assertThat(h.proponerVisita(3L, "el sabado", null)).contains("formato");
        assertThat(h.accionPropuesta()).isNull();
        verifyNoInteractions(accionRepository);
    }
}
