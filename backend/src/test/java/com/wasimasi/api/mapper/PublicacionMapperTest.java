package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.RolUsuario;
import com.wasimasi.api.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del mapper generado por MapStruct (US08, US09, US10 y US11):
 * comprueban que el DTO de salida no exponga entidades y que el sello de
 * verificacion solo se encienda para el estado VERIFICADO.
 */
class PublicacionMapperTest {

    private final PublicacionMapper mapper = new PublicacionMapperImpl();

    private static Usuario anfitrion(EstadoVerificacion estado) {
        return Usuario.builder()
                .id(10L)
                .nombre("Ana")
                .correo("ana@upc.edu.pe")
                .rol(RolUsuario.ANFITRION)
                .estadoVerificacion(estado)
                .build();
    }

    private static Publicacion publicacion(EstadoVerificacion estado) {
        return Publicacion.builder()
                .id(3L)
                .anfitrion(anfitrion(estado))
                .zona("Miraflores")
                .precio(new BigDecimal("900.00"))
                .descripcion("Habitacion luminosa")
                .fotos(List.of("http://imagenes/1.jpg"))
                .estado(EstadoPublicacion.ACTIVA)
                .fechaPublicacion(LocalDateTime.of(2026, 11, 20, 10, 30))
                .mascotasPermitidas(false)
                .generoPreferido("F")
                .servicios("agua,wifi")
                .condicionesNoNegociables("genero,servicios")
                .latitud(new BigDecimal("-12.100000"))
                .longitud(new BigDecimal("-77.050000"))
                .build();
    }

    @Test
    @DisplayName("US08/US09/US10 - mapea anfitrion verificado, coordenadas y condiciones")
    void mapteaTodosLosCamposDelDTO() {
        Publicacion publicacion = publicacion(EstadoVerificacion.VERIFICADO);

        PublicacionResponseDTO dto = mapper.toResponseDTO(publicacion);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getAnfitrionId()).isEqualTo(10L);
        assertThat(dto.getAnfitrionNombre()).isEqualTo("Ana");
        assertThat(dto.isAnfitrionVerificado()).isTrue();
        assertThat(dto.getZona()).isEqualTo("Miraflores");
        assertThat(dto.getPrecio()).isEqualByComparingTo("900.00");
        assertThat(dto.getDescripcion()).isEqualTo("Habitacion luminosa");
        assertThat(dto.getFotos()).containsExactly("http://imagenes/1.jpg");
        assertThat(dto.getEstado()).isEqualTo(EstadoPublicacion.ACTIVA);
        assertThat(dto.getFechaPublicacion()).isEqualTo(LocalDateTime.of(2026, 11, 20, 10, 30));
        assertThat(dto.getMascotasPermitidas()).isFalse();
        assertThat(dto.getGeneroPreferido()).isEqualTo("F");
        assertThat(dto.getServicios()).isEqualTo("agua,wifi");
        assertThat(dto.getCondicionesNoNegociables()).containsExactly("genero", "servicios");
        assertThat(dto.getLatitud()).isEqualByComparingTo("-12.100000");
        assertThat(dto.getLongitud()).isEqualByComparingTo("-77.050000");
    }

    @ParameterizedTest(name = "estado {0} => anfitrionVerificado=false")
    @EnumSource(value = EstadoVerificacion.class, names = {"PENDIENTE", "RECHAZADO"})
    @DisplayName("US08 - el sello de verificacion solo se activa con VERIFICADO")
    void anfitrionNoVerificadoNoLlevaSello(EstadoVerificacion estado) {
        PublicacionResponseDTO dto = mapper.toResponseDTO(publicacion(estado));

        assertThat(dto.isAnfitrionVerificado()).isFalse();
        assertThat(dto.getAnfitrionId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("US09 - publicacion sin campo de condiciones (anterior a US09) mantiene null")
    void condicionesNoNegociablesAusentesSeMapeanComoNull() {
        Publicacion publicacion = publicacion(EstadoVerificacion.VERIFICADO);
        publicacion.setCondicionesNoNegociables(null);

        PublicacionResponseDTO dto = mapper.toResponseDTO(publicacion);

        assertThat(dto.getCondicionesNoNegociables()).isNull();
    }

    @Test
    @DisplayName("US11 - una publicacion sin anfitrion no rompe el mapeo")
    void mapteaSinAnfitrion() {
        Publicacion publicacion = publicacion(EstadoVerificacion.VERIFICADO);
        publicacion.setAnfitrion(null);

        PublicacionResponseDTO dto = mapper.toResponseDTO(publicacion);

        assertThat(dto.getAnfitrionId()).isNull();
        assertThat(dto.getAnfitrionNombre()).isNull();
        assertThat(dto.isAnfitrionVerificado()).isFalse();
        assertThat(dto.getId()).isEqualTo(3L);
    }
}
