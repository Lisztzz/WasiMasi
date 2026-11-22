package com.wasimasi.api.service;

import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.PublicacionMapper;
import com.wasimasi.api.mapper.PublicacionMapperImpl;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.RolUsuario;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de US06 (publicar), US08 (buscar), US09 (condiciones) y
 * US11 (detalle). No arrancan contexto de Spring ni usan base de datos.
 */
@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    private static final Long ANFITRION_ID = 10L;
    private static final BigDecimal PRECIO = new BigDecimal("850.00");
    private static final BigDecimal PRECIO_MIN = new BigDecimal("500.00");
    private static final BigDecimal PRECIO_MAX = new BigDecimal("1000.00");

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    /** Mapper real generado por MapStruct: asi se comprueba tambien la salida. */
    @Spy
    private PublicacionMapper publicacionMapper = new PublicacionMapperImpl();

    @InjectMocks
    private PublicacionService publicacionService;

    private static Usuario anfitrion(EstadoVerificacion estado) {
        return Usuario.builder()
                .id(ANFITRION_ID)
                .nombre("Ana")
                .correo("ana@upc.edu.pe")
                .rol(RolUsuario.ANFITRION)
                .estadoVerificacion(estado)
                .build();
    }

    private static PublicacionRequestDTO request() {
        PublicacionRequestDTO request = new PublicacionRequestDTO();
        request.setZona("Miraflores");
        request.setPrecio(PRECIO);
        request.setDescripcion("Habitacion luminosa");
        request.setFotos(List.of("http://imagenes/1.jpg"));
        request.setMascotasPermitidas(true);
        request.setGeneroPreferido("f");
        request.setServicios("Agua, Wifi");
        request.setLatitud(new BigDecimal("-12.120000"));
        request.setLongitud(new BigDecimal("-77.030000"));
        return request;
    }

    /** Publicacion de prueba con el estado que exige US08 (solo ACTIVA). */
    private static Publicacion publicacion(Long id, Boolean mascotas, String genero, String servicios) {
        return publicacion(id, mascotas, genero, servicios, null);
    }

    /** US09 - Ultimo parámetro: CSV de condiciones rígidas; null = publicacion anterior a US09. */
    private static Publicacion publicacion(Long id, Boolean mascotas, String genero, String servicios,
                                           String condicionesNoNegociables) {
        return Publicacion.builder()
                .id(id)
                .anfitrion(anfitrion(EstadoVerificacion.VERIFICADO))
                .zona("Miraflores")
                .precio(PRECIO)
                .fotos(List.of("http://imagenes/1.jpg"))
                .estado(EstadoPublicacion.ACTIVA)
                .mascotasPermitidas(mascotas)
                .generoPreferido(genero)
                .servicios(servicios)
                .condicionesNoNegociables(condicionesNoNegociables)
                .build();
    }

    private void simularGuardado() {
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(invocacion -> {
            Publicacion entidad = invocacion.getArgument(0);
            entidad.setId(99L);
            return entidad;
        });
    }

    private List<PublicacionResponseDTO> buscarCon(Boolean mascotas, String genero, String servicios) {
        when(publicacionRepository.findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
                eq(EstadoPublicacion.ACTIVA), eq("Miraflores"), eq(PRECIO_MIN), eq(PRECIO_MAX)))
                .thenReturn(List.of(
                        publicacion(1L, true, "F", "Agua, Wifi"),
                        publicacion(2L, false, "M", "agua"),
                        publicacion(3L, true, "Any", "WIFI, Electricidad"),
                        publicacion(4L, null, null, null)));

        return publicacionService.buscar("Miraflores", PRECIO_MIN, PRECIO_MAX, mascotas, genero, servicios);
    }

    // ------------------------------------------------------------------ US06

    @Test
    @DisplayName("US06 - anfitrion verificado: la publicacion queda ACTIVA y guarda US09/US10")
    void publicarConAnfitrionVerificadoGuardaActiva() {
        Usuario verificada = anfitrion(EstadoVerificacion.VERIFICADO);
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.of(verificada));
        simularGuardado();

        PublicacionResponseDTO respuesta = publicacionService.publicar(ANFITRION_ID, request());

        assertThat(respuesta.getEstado()).isEqualTo(EstadoPublicacion.ACTIVA);
        assertThat(respuesta.isAnfitrionVerificado()).isTrue();
        assertThat(respuesta.getAdvertencia()).isNull();
        assertThat(respuesta.getAnfitrionId()).isEqualTo(ANFITRION_ID);
        assertThat(respuesta.getAnfitrionNombre()).isEqualTo("Ana");
        assertThat(respuesta.getZona()).isEqualTo("Miraflores");
        assertThat(respuesta.getPrecio()).isEqualByComparingTo(PRECIO);
        assertThat(respuesta.getMascotasPermitidas()).isTrue();
        assertThat(respuesta.getGeneroPreferido()).isEqualTo("F");
        assertThat(respuesta.getServicios()).isEqualTo("Agua, Wifi");
        assertThat(respuesta.getLatitud()).isEqualByComparingTo("-12.120000");
        assertThat(respuesta.getLongitud()).isEqualByComparingTo("-77.030000");

        ArgumentCaptor<Publicacion> captor = ArgumentCaptor.forClass(Publicacion.class);
        verify(publicacionRepository).save(captor.capture());
        Publicacion guardada = captor.getValue();
        assertThat(guardada.getAnfitrion()).isSameAs(verificada);
        assertThat(guardada.getEstado()).isEqualTo(EstadoPublicacion.ACTIVA);
        assertThat(guardada.getGeneroPreferido()).isEqualTo("F");
        assertThat(guardada.getFotos()).containsExactly("http://imagenes/1.jpg");
    }

    @ParameterizedTest(name = "estado {0} => BORRADOR con advertencia")
    @EnumSource(value = EstadoVerificacion.class, names = {"PENDIENTE", "RECHAZADO"})
    @DisplayName("US06 - anfitrion sin verificar: queda BORRADOR y avisa")
    void publicarSinVerificarGuardaBorradorConAdvertencia(EstadoVerificacion estado) {
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.of(anfitrion(estado)));
        simularGuardado();

        PublicacionResponseDTO respuesta = publicacionService.publicar(ANFITRION_ID, request());

        assertThat(respuesta.getEstado()).isEqualTo(EstadoPublicacion.BORRADOR);
        assertThat(respuesta.isAnfitrionVerificado()).isFalse();
        assertThat(respuesta.getAdvertencia()).contains("Verifica tu identidad");
    }

    @Test
    @DisplayName("US06 - sin fotos (null) no se puede publicar")
    void publicarSinFotosNullLanzaBadRequest() {
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.of(anfitrion(EstadoVerificacion.VERIFICADO)));
        PublicacionRequestDTO request = request();
        request.setFotos(null);

        assertThatThrownBy(() -> publicacionService.publicar(ANFITRION_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Agrega al menos una foto para publicar");

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("US06 - sin fotos (lista vacia) no se puede publicar")
    void publicarSinFotosLanzaBadRequest() {
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.of(anfitrion(EstadoVerificacion.VERIFICADO)));
        PublicacionRequestDTO request = request();
        request.setFotos(List.of());

        assertThatThrownBy(() -> publicacionService.publicar(ANFITRION_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("al menos una foto");

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("US06 - no existe el anfitrion autenticado")
    void publicarSinAnfitrionLanzaResourceNotFound() {
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicacionService.publicar(ANFITRION_ID, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("anfitrion");

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("US09 - genero invalido al publicar")
    void publicarConGeneroInvalidoLanzaBadRequest() {
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.of(anfitrion(EstadoVerificacion.VERIFICADO)));
        PublicacionRequestDTO request = request();
        request.setGeneroPreferido("X");

        assertThatThrownBy(() -> publicacionService.publicar(ANFITRION_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El genero debe ser M, F o Any");

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("US09 - publicar guarda una sola lista de condiciones no negociables")
    void publicarGuardaCondicionesNoNegociables() {
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.of(anfitrion(EstadoVerificacion.VERIFICADO)));
        simularGuardado();
        PublicacionRequestDTO request = request();
        request.setCondicionesNoNegociables(List.of("genero", "mascotas", "genero", "  SERVICIOS "));

        PublicacionResponseDTO respuesta = publicacionService.publicar(ANFITRION_ID, request);

        assertThat(respuesta.getCondicionesNoNegociables()).containsExactly("mascotas", "genero", "servicios");
        assertThat(respuesta.getCondicionesNegociablesIncumplidas()).isEmpty();

        ArgumentCaptor<Publicacion> captor = ArgumentCaptor.forClass(Publicacion.class);
        verify(publicacionRepository).save(captor.capture());
        assertThat(captor.getValue().getCondicionesNoNegociables()).isEqualTo("mascotas,genero,servicios");
    }

    @Test
    @DisplayName("US09 - una condición desconocida al publicar es error")
    void publicarConCondicionInvalidaLanzaBadRequest() {
        when(usuarioRepository.findById(ANFITRION_ID)).thenReturn(Optional.of(anfitrion(EstadoVerificacion.VERIFICADO)));
        PublicacionRequestDTO request = request();
        request.setCondicionesNoNegociables(List.of("mascotas", "mascota"));

        assertThatThrownBy(() -> publicacionService.publicar(ANFITRION_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no es valida");

        verify(publicacionRepository, never()).save(any());
    }

    // --------------------------------------------------------------- US08/09

    @Test
    @DisplayName("US08 - sin filtros opcionales devuelve todas las publicaciones activas")
    void buscarSinFiltrosDevuelveTodas() {
        List<Publicacion> encontradas = List.of(
                publicacion(1L, true, "M", "agua,wifi"),
                publicacion(2L, false, "Any", null));
        when(publicacionRepository.findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
                eq(EstadoPublicacion.ACTIVA), eq("Miraflores"), eq(PRECIO_MIN), eq(PRECIO_MAX)))
                .thenReturn(encontradas);

        List<PublicacionResponseDTO> resultado = publicacionService.buscar(
                "Miraflores", PRECIO_MIN, PRECIO_MAX, null, null, null);

        assertThat(resultado).extracting(PublicacionResponseDTO::getId).containsExactly(1L, 2L);
        assertThat(resultado).allSatisfy(dto ->
                assertThat(dto.getEstado()).isEqualTo(EstadoPublicacion.ACTIVA));
    }

    @Test
    @DisplayName("US08 - precios no enviados se pasan como nulos al repositorio")
    void buscarSinPreciosPasaNulosAlRepositorio() {
        when(publicacionRepository.findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
                eq(EstadoPublicacion.ACTIVA), eq("Miraflores"), isNull(), isNull()))
                .thenReturn(List.of(publicacion(1L, true, "M", null)));

        List<PublicacionResponseDTO> resultado = publicacionService.buscar(
                "Miraflores", null, null, null, null, null);

        assertThat(resultado).extracting(PublicacionResponseDTO::getId).containsExactly(1L);
    }

    @Test
    @DisplayName("US08 - presupuesto minimo mayor al maximo es error")
    void buscarConPrecioInvertidoLanzaBadRequest() {
        assertThatThrownBy(() -> publicacionService.buscar(
                "Miraflores", PRECIO_MAX, PRECIO_MIN, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El presupuesto minimo no puede ser mayor al maximo");

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    @DisplayName("US09 - filtro de mascotas solo incluye el valor pedido")
    void buscarFiltraPorMascotas() {
        assertThat(buscarCon(true, null, null))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L, 3L);
        assertThat(buscarCon(false, null, null))
                .extracting(PublicacionResponseDTO::getId).containsExactly(2L);
    }

    @Test
    @DisplayName("US09 - genero se normaliza (f => F) e incluye publicaciones Any")
    void buscarFiltraPorGeneroNormalizado() {
        assertThat(buscarCon(null, "f", null))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L, 3L);
        assertThat(buscarCon(null, "M", null))
                .extracting(PublicacionResponseDTO::getId).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("US09 - genero Any no filtra")
    void buscarConGeneroAnyNoFiltra() {
        assertThat(buscarCon(null, "Any", null))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L, 2L, 3L, 4L);
        assertThat(buscarCon(null, "  any  ", null))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("US09 - genero invalido al buscar es error")
    void buscarConGeneroInvalidoLanzaBadRequest() {
        assertThatThrownBy(() -> publicacionService.buscar(
                "Miraflores", PRECIO_MIN, PRECIO_MAX, null, "X", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El genero debe ser M, F o Any");

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    @DisplayName("US09 - servicios coma-separados, sin importar mayusculas o espacios")
    void buscarFiltraPorServicios() {
        assertThat(buscarCon(null, null, "agua,wifi"))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L);
        assertThat(buscarCon(null, null, " WIFI , Agua "))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L);
        assertThat(buscarCon(null, null, "   "))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("US09 - se pueden combinar mascotas, genero y servicios")
    void buscarCombinaFiltros() {
        assertThat(buscarCon(true, "f", "wifi"))
                .extracting(PublicacionResponseDTO::getId).containsExactly(1L, 3L);
        assertThat(buscarCon(true, "m", "electricidad"))
                .extracting(PublicacionResponseDTO::getId).containsExactly(3L);
    }

    @Test
    @DisplayName("US09 - solo excluye condiciones rígidas; las negociables se devuelven con aviso")
    void buscarSoloExcluyeCondicionesNoNegociables() {
        when(publicacionRepository.findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
                eq(EstadoPublicacion.ACTIVA), eq("Miraflores"), eq(PRECIO_MIN), eq(PRECIO_MAX)))
                .thenReturn(List.of(
                        publicacion(1L, true, "Any", "wifi", "mascotas"),
                        publicacion(2L, false, "Any", "wifi", "mascotas,genero,servicios"),
                        publicacion(3L, true, "F", "electricidad", "")));

        List<PublicacionResponseDTO> resultado = publicacionService.buscar(
                "Miraflores", PRECIO_MIN, PRECIO_MAX, false, "F", "wifi");

        assertThat(resultado).extracting(PublicacionResponseDTO::getId).containsExactly(2L, 3L);
        PublicacionResponseDTO negociable = resultado.get(1);
        assertThat(negociable.getCondicionesNoNegociables()).isEmpty();
        assertThat(negociable.getCondicionesNegociablesIncumplidas())
                .containsExactly("mascotas", "servicios");
        assertThat(resultado.get(0).getCondicionesNegociablesIncumplidas()).isEmpty();
    }

    @Test
    @DisplayName("US09 - sin condiciones rígidas ningún filtro excluye y todo se avisa")
    void buscarTodoNegociableNoExcluyeNada() {
        when(publicacionRepository.findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
                eq(EstadoPublicacion.ACTIVA), eq("Miraflores"), eq(PRECIO_MIN), eq(PRECIO_MAX)))
                .thenReturn(List.of(publicacion(5L, false, "M", "agua", "")));

        List<PublicacionResponseDTO> resultado = publicacionService.buscar(
                "Miraflores", PRECIO_MIN, PRECIO_MAX, true, "F", "wifi");

        assertThat(resultado).extracting(PublicacionResponseDTO::getId).containsExactly(5L);
        assertThat(resultado.get(0).getCondicionesNegociablesIncumplidas())
                .containsExactly("mascotas", "genero", "servicios");
    }

    // ------------------------------------------------------------------ US11

    @Test
    @DisplayName("US11 - ver detalle de una publicacion existente")
    void verDetalleExistenteDevuelveDTO() {
        when(publicacionRepository.findById(1L))
                .thenReturn(Optional.of(publicacion(1L, true, "F", "agua,wifi")));

        PublicacionResponseDTO respuesta = publicacionService.verDetalle(1L);

        assertThat(respuesta.getId()).isEqualTo(1L);
        assertThat(respuesta.getAnfitrionId()).isEqualTo(ANFITRION_ID);
        assertThat(respuesta.getZona()).isEqualTo("Miraflores");
        assertThat(respuesta.getMascotasPermitidas()).isTrue();
        assertThat(respuesta.getGeneroPreferido()).isEqualTo("F");
    }

    @Test
    @DisplayName("US11 - ver detalle de una publicacion inexistente")
    void verDetalleInexistenteLanzaResourceNotFound() {
        when(publicacionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicacionService.verDetalle(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontro la publicacion con id 404");
    }
}
