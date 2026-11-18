package com.wasimasi.api.service;

import com.wasimasi.api.dto.EditarPublicacionRequestDTO;
import com.wasimasi.api.dto.EstadoPublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ForbiddenException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.PublicacionMapper;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio de US06 (publicar), US08 (buscar) y US11 (ver detalle).
 */
@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionMapper publicacionMapper;

    /**
     * @param anfitrionId id del usuario autenticado (viene del JWT, no del body).
     *                    El rol ANFITRION ya fue validado por SecurityConfig.
     */
    public PublicacionResponseDTO publicar(Long anfitrionId, PublicacionRequestDTO request) {
        Usuario anfitrion = usuarioRepository.findById(anfitrionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el anfitrion con id " + anfitrionId));

        if (request.getFotos() == null || request.getFotos().isEmpty()) {
            throw new BadRequestException("Agrega al menos una foto para publicar");
        }

        boolean anfitrionVerificado = anfitrion.getEstadoVerificacion() == EstadoVerificacion.VERIFICADO;

        Publicacion publicacion = Publicacion.builder()
                .anfitrion(anfitrion)
                .zona(request.getZona())
                .precio(request.getPrecio())
                .descripcion(request.getDescripcion())
                .fotos(request.getFotos())
                .estado(anfitrionVerificado ? EstadoPublicacion.ACTIVA : EstadoPublicacion.BORRADOR)
                .build();

        Publicacion guardada = publicacionRepository.save(publicacion);
        PublicacionResponseDTO response = publicacionMapper.toResponseDTO(guardada);

        if (!anfitrionVerificado) {
            response.setAdvertencia("Verifica tu identidad para publicar");
        }
        return response;
    }

    /** US07 - Listar las publicaciones del anfitrión en todos sus estados. */
    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> misPublicaciones(Long anfitrionId) {
        return publicacionRepository.findByAnfitrionIdOrderByFechaPublicacionDescIdDesc(anfitrionId)
                .stream()
                .map(publicacionMapper::toResponseDTO)
                .toList();
    }

    /** US07 - Editar los datos de una publicación propia. */
    @Transactional
    public PublicacionResponseDTO editar(Long anfitrionId, Long publicacionId, EditarPublicacionRequestDTO request) {
        Publicacion publicacion = obtenerPublicacionPropia(anfitrionId, publicacionId);
        publicacion.setTitulo(request.titulo());
        publicacion.setZona(request.zona());
        publicacion.setPrecio(request.precio());
        publicacion.setDescripcion(request.descripcion());
        publicacion.getFotos().clear();
        publicacion.getFotos().addAll(request.fotos());
        return publicacionMapper.toResponseDTO(publicacion);
    }

    /** US07 y US18 - Pausar, activar o marcar como ocupada una publicación propia. */
    @Transactional
    public PublicacionResponseDTO cambiarEstado(Long anfitrionId, Long publicacionId,
                                                EstadoPublicacionRequestDTO request) {
        Publicacion publicacion = obtenerPublicacionPropia(anfitrionId, publicacionId);
        EstadoPublicacion estado = EstadoPublicacion.valueOf(request.estado());
        if (estado == EstadoPublicacion.ACTIVA
                && publicacion.getAnfitrion().getEstadoVerificacion() != EstadoVerificacion.VERIFICADO) {
            throw new ForbiddenException("Verifica tu identidad para activar la publicación");
        }
        publicacion.setEstado(estado);
        return publicacionMapper.toResponseDTO(publicacion);
    }

    private Publicacion obtenerPublicacionPropia(Long anfitrionId, Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findPublicacionById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación con id " + publicacionId));
        if (!publicacion.getAnfitrion().getId().equals(anfitrionId)) {
            throw new ForbiddenException("Solo puedes modificar tus propias publicaciones");
        }
        return publicacion;
    }

    /** US08 y US18 - Buscar solo publicaciones activas con los filtros solicitados. */
    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> buscar(String zona, BigDecimal precioMin, BigDecimal precioMax) {
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new BadRequestException("El presupuesto minimo no puede ser mayor al maximo");
        }

        return publicacionRepository
                .findAll((publicacion, consulta, criterios) -> {
                    List<Predicate> filtros = new ArrayList<>();
                    filtros.add(criterios.equal(publicacion.get("estado"), EstadoPublicacion.ACTIVA));
                    if (zona != null) {
                        filtros.add(criterios.equal(criterios.lower(publicacion.get("zona")),
                                criterios.lower(criterios.literal(zona))));
                    }
                    if (precioMin != null) {
                        filtros.add(criterios.greaterThanOrEqualTo(publicacion.get("precio"), precioMin));
                    }
                    if (precioMax != null) {
                        filtros.add(criterios.lessThanOrEqualTo(publicacion.get("precio"), precioMax));
                    }
                    return criterios.and(filtros.toArray(Predicate[]::new));
                })
                .stream()
                .map(publicacionMapper::toResponseDTO)
                .toList();
    }

    public PublicacionResponseDTO verDetalle(Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la publicacion con id " + publicacionId));
        return publicacionMapper.toResponseDTO(publicacion);
    }
}
