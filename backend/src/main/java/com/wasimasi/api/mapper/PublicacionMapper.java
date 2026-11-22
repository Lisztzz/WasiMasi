package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.Publicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/** Mapea publicaciones a DTOs de salida (US08, US09, US11). */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PublicacionMapper {

    @Mapping(target = "anfitrionId", source = "anfitrion.id")
    @Mapping(target = "anfitrionNombre", source = "anfitrion.nombre")
    @Mapping(target = "anfitrionVerificado", source = "anfitrion.estadoVerificacion", qualifiedByName = "verificado")
    PublicacionResponseDTO toResponseDTO(Publicacion publicacion);

    /** Un anfitrión solo se considera verificado cuando su estado es VERIFICADO. */
    @Named("verificado")
    default boolean esVerificado(EstadoVerificacion estado) {
        return estado == EstadoVerificacion.VERIFICADO;
    }
}
