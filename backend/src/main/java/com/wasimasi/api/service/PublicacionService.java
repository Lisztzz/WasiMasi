package com.wasimasi.api.service;

import com.wasimasi.api.dto.PublicacionRequestDTO;
import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.PublicacionMapper;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public PublicacionResponseDTO publicar(PublicacionRequestDTO request) {
        Usuario anfitrion = usuarioRepository.findById(request.getAnfitrionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el anfitrion con id " + request.getAnfitrionId()));

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
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new BadRequestException("El presupuesto minimo no puede ser mayor al maximo");
        }

        return publicacionRepository
                .findByEstadoAndZonaIgnoreCaseAndPrecioBetween(EstadoPublicacion.ACTIVA, zona, precioMin, precioMax)
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
