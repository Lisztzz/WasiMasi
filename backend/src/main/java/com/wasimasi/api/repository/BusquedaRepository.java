package com.wasimasi.api.repository;

import com.wasimasi.api.dto.ConteoRangoPrecioDTO;
import com.wasimasi.api.dto.ConteoZonaDTO;
import com.wasimasi.api.model.Busqueda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** US20 - Agregaciones de búsquedas registradas por US08. */
public interface BusquedaRepository extends JpaRepository<Busqueda, Long> {

    @Query("""
            select new com.wasimasi.api.dto.ConteoZonaDTO(b.zona, count(b))
            from Busqueda b
            group by b.zona
            order by count(b) desc
            """)
    List<ConteoZonaDTO> contarPorZona();

    @Query("""
            select new com.wasimasi.api.dto.ConteoRangoPrecioDTO(b.precioMin, b.precioMax, count(b))
            from Busqueda b
            group by b.precioMin, b.precioMax
            order by count(b) desc
            """)
    List<ConteoRangoPrecioDTO> contarPorRangoPrecio();
}
