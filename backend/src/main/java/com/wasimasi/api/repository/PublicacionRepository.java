package com.wasimasi.api.repository;

import com.wasimasi.api.model.Publicacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long>,
        JpaSpecificationExecutor<Publicacion> {

    /** US07 - Listar las publicaciones propias, incluidas las no disponibles. */
    List<Publicacion> findByAnfitrionIdOrderByFechaPublicacionDescIdDesc(Long anfitrionId);

    /** US07 y US18 - Serializar los cambios de datos y estado de una misma publicación. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Publicacion> findPublicacionById(Long id);
}
