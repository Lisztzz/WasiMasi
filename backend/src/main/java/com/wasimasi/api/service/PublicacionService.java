package com.wasimasi.api.service;

import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.dto.BusquedaRequestDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.PublicacionMapper;
import com.wasimasi.api.model.Busqueda;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.BusquedaRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas de negocio de US06 (publicar), US08 (buscar), US11 (ver detalle) y US20 (agregar demanda).
 */
@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final BusquedaRepository busquedaRepository;
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

    public List<PublicacionResponseDTO> buscar(String zona, BigDecimal precioMin, BigDecimal precioMax) {
        validarFiltros(zona, precioMin, precioMax);
        return buscarPublicaciones(zona.trim(), precioMin, precioMax);
    }

    /** US08/US20 - Registra zona y presupuesto sin conservar identidad personal. */
    @Transactional
    public List<PublicacionResponseDTO> buscarYRegistrar(BusquedaRequestDTO request) {
        validarFiltros(request.getZona(), request.getPrecioMin(), request.getPrecioMax());
        String zona = request.getZona().trim();
        busquedaRepository.save(new Busqueda(zona, request.getPrecioMin(), request.getPrecioMax()));
        return buscarPublicaciones(zona, request.getPrecioMin(), request.getPrecioMax());
    }

    private List<PublicacionResponseDTO> buscarPublicaciones(String zona, BigDecimal precioMin, BigDecimal precioMax) {
        return publicacionRepository
                .findByEstadoAndZonaIgnoreCaseAndPrecioBetween(EstadoPublicacion.ACTIVA, zona, precioMin, precioMax)
                .stream()
                .map(publicacionMapper::toResponseDTO)
                .toList();
    }

    private void validarFiltros(String zona, BigDecimal precioMin, BigDecimal precioMax) {
        if (zona == null || zona.isBlank()) {
            throw new BadRequestException("Indica una zona para buscar");
        }
        if (precioMin == null || precioMax == null || precioMin.signum() < 0 || precioMax.signum() < 0) {
            throw new BadRequestException("Indica un rango de precios válido");
        }
        if (precioMin.compareTo(precioMax) > 0) {
            throw new BadRequestException("El presupuesto minimo no puede ser mayor al maximo");
        }
    }

    public PublicacionResponseDTO verDetalle(Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la publicacion con id " + publicacionId));
        return publicacionMapper.toResponseDTO(publicacion);
    }

}
