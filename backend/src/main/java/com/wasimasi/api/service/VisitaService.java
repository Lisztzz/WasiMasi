package com.wasimasi.api.service;

import com.wasimasi.api.dto.InteresadoComparacionDTO;
import com.wasimasi.api.dto.VisitaRequestDTO;
import com.wasimasi.api.dto.VisitaResponseDTO;
import com.wasimasi.api.dto.VisitaRespuestaRequestDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ConflictException;
import com.wasimasi.api.exception.ForbiddenException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.VisitaMapper;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.EstadoVisita;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.model.Visita;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import com.wasimasi.api.repository.VisitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio de US16, US17 y US19. */
@Service
@RequiredArgsConstructor
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final VisitaMapper visitaMapper;

    /** US16 - Registrar una solicitud de visita de un buscador. */
    @Transactional
    public VisitaResponseDTO solicitar(Long buscadorId, VisitaRequestDTO request) {
        Publicacion publicacion = publicacionRepository.findById(request.getPublicacionId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación"));

        if (publicacion.getEstado() != EstadoPublicacion.ACTIVA) {
            throw new BadRequestException("La publicación ya no está disponible para visitas");
        }
        if (publicacion.getAnfitrion().getId().equals(buscadorId)) {
            throw new BadRequestException("No puedes solicitar una visita a tu propia publicación");
        }
        if (!request.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de visita debe ser futura");
        }
        if (visitaRepository.existsByBuscadorIdAndPublicacionIdAndEstado(
                buscadorId, publicacion.getId(), EstadoVisita.PENDIENTE)) {
            throw new ConflictException("Ya tienes una solicitud pendiente para esta publicación");
        }

        Usuario buscador = usuarioRepository.findById(buscadorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario"));
        Visita visita = Visita.builder()
                .buscador(buscador)
                .publicacion(publicacion)
                .fechaHora(request.getFechaHora())
                .comentario(limpiar(request.getComentario()))
                .estado(EstadoVisita.PENDIENTE)
                .creadaEn(LocalDateTime.now())
                .build();

        return visitaMapper.toDto(visitaRepository.save(visita));
    }

    /** US17 - Responder una solicitud pendiente de una publicación propia. */
    @Transactional
    public VisitaResponseDTO responder(Long anfitrionId, Long visitaId, VisitaRespuestaRequestDTO request) {
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la solicitud de visita"));
        if (!visita.getPublicacion().getAnfitrion().getId().equals(anfitrionId)) {
            throw new ForbiddenException("No puedes responder una visita de otra persona");
        }
        if (visita.getEstado() != EstadoVisita.PENDIENTE) {
            throw new ConflictException("Esta solicitud de visita ya fue respondida");
        }
        if (request.getEstado() != EstadoVisita.CONFIRMADA && request.getEstado() != EstadoVisita.RECHAZADA) {
            throw new BadRequestException("La respuesta debe confirmar o rechazar la visita");
        }
        if (request.getEstado() == EstadoVisita.RECHAZADA && limpiar(request.getMotivoRespuesta()) == null) {
            throw new BadRequestException("Indica el motivo del rechazo");
        }

        visita.setEstado(request.getEstado());
        visita.setMotivoRespuesta(request.getEstado() == EstadoVisita.RECHAZADA
                ? limpiar(request.getMotivoRespuesta()) : null);
        visita.setActualizadaEn(LocalDateTime.now());
        return visitaMapper.toDto(visitaRepository.save(visita));
    }

    /** US17 - Listar las visitas recibidas por el anfitrión autenticado. */
    @Transactional(readOnly = true)
    public List<VisitaResponseDTO> misVisitasComoAnfitrion(Long anfitrionId) {
        return visitaRepository.findByPublicacionAnfitrionIdOrderByCreadaEnDesc(anfitrionId)
                .stream()
                .map(visitaMapper::toDto)
                .toList();
    }

    /** US19 - Comparar las personas que solicitaron visitar una publicación propia. */
    @Transactional(readOnly = true)
    public List<InteresadoComparacionDTO> compararInteresados(Long anfitrionId, Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación"));
        if (!publicacion.getAnfitrion().getId().equals(anfitrionId)) {
            throw new ForbiddenException("No puedes consultar interesados de otra publicación");
        }

        return visitaRepository.findByPublicacionIdOrderByCreadaEnDesc(publicacionId).stream()
                .map(visita -> InteresadoComparacionDTO.builder()
                        .visitaId(visita.getId())
                        .buscadorId(visita.getBuscador().getId())
                        .nombre(visita.getBuscador().getNombre())
                        .universidad(visita.getBuscador().getUniversidad())
                        .estadoVerificacion(visita.getBuscador().getEstadoVerificacion())
                        .fechaRegistro(visita.getBuscador().getFechaCreacion())
                        .fechaSolicitud(visita.getCreadaEn())
                        .fechaHoraVisita(visita.getFechaHora())
                        .estadoVisita(visita.getEstado().name())
                        .comentario(visita.getComentario())
                        .build())
                .toList();
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
