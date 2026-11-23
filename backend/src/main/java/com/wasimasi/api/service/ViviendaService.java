package com.wasimasi.api.service;

import com.wasimasi.api.dto.ViviendaRequestDTO;
import com.wasimasi.api.dto.ViviendaResponseDTO;
import com.wasimasi.api.exception.ConflictException;
import com.wasimasi.api.exception.ForbiddenException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.GrupoMapper;
import com.wasimasi.api.model.EstadoVisita;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.Vivienda;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import com.wasimasi.api.repository.VisitaRepository;
import com.wasimasi.api.repository.ViviendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** US15 - Permite al anfitrión agrupar sus habitaciones, incluso las ya publicadas. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViviendaService {
    private final ViviendaRepository viviendas;
    private final PublicacionRepository publicaciones;
    private final UsuarioRepository usuarios;
    private final VisitaRepository visitas;
    private final GrupoService grupos;
    private final GrupoMapper mapper;

    public List<ViviendaResponseDTO> listar(Long anfitrionId) {
        return viviendas.findByAnfitrionIdOrderByNombreAsc(anfitrionId).stream()
                .map(mapper::toViviendaDTO)
                .toList();
    }

    @Transactional
    public ViviendaResponseDTO crear(Long anfitrionId, ViviendaRequestDTO request) {
        return mapper.toViviendaDTO(viviendas.save(
                new Vivienda(usuarios.getReferenceById(anfitrionId), request.nombre().strip())));
    }

    @Transactional
    public ViviendaResponseDTO vincular(Long anfitrionId, Long publicacionId, Long viviendaId) {
        Publicacion publicacion = publicaciones.findPublicacionById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación"));
        Vivienda vivienda = grupos.bloquear(viviendaId);
        if (!publicacion.getAnfitrion().getId().equals(anfitrionId)
                || !vivienda.getAnfitrion().getId().equals(anfitrionId)) {
            throw new ForbiddenException("Solo puedes vincular tus publicaciones y viviendas");
        }
        if (publicacion.getVivienda() != null) {
            if (publicacion.getVivienda().getId().equals(viviendaId)) return mapper.toViviendaDTO(vivienda);
            throw new ConflictException("La habitación ya está vinculada a otra vivienda");
        }
        publicacion.setVivienda(vivienda);
        visitas.findByPublicacionIdAndEstado(publicacionId, EstadoVisita.CONFIRMADA)
                .forEach(visita -> grupos.incorporarVisitaConfirmada(vivienda, visita.getBuscador()));
        return mapper.toViviendaDTO(vivienda);
    }
}
