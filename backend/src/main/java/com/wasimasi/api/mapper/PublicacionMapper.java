package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.Publicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Mapea publicaciones a DTOs de salida (US08, US09, US11). */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PublicacionMapper {

    @Mapping(target = "anfitrionId", source = "anfitrion.id")
    @Mapping(target = "anfitrionNombre", source = "anfitrion.nombre")
    @Mapping(target = "anfitrionVerificado", source = "anfitrion.estadoVerificacion", qualifiedByName = "verificado")
    @Mapping(target = "viviendaId", source = "vivienda.id")
    @Mapping(target = "condicionesNoNegociables", source = "condicionesNoNegociables", qualifiedByName = "csvALista")
    PublicacionResponseDTO toResponseDTO(Publicacion publicacion);

    /** US09 - La columna guarda las claves separadas por coma; la API las expone como lista. */
    @Named("csvALista")
    default List<String> csvALista(String csv) {
        if (csv == null) {
            return null; // Publicacion anterior a US09: el buscador la trata como rigida.
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(clave -> !clave.isEmpty())
                .map(clave -> clave.toLowerCase(Locale.ROOT))
                .toList();
    }

    /** Un anfitrión solo se considera verificado cuando su estado es VERIFICADO. */
    @Named("verificado")
    default boolean esVerificado(EstadoVerificacion estado) {
        return estado == EstadoVerificacion.VERIFICADO;
    }
}
