package com.wasimasi.api.repository;

import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    /** US08 - Buscar habitaciones por zona y presupuesto, solo entre las publicaciones activas. */
    List<Publicacion> findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
            EstadoPublicacion estado, String zona, BigDecimal precioMin, BigDecimal precioMax);
}
