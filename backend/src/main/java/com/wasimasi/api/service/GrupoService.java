package com.wasimasi.api.service;

import com.wasimasi.api.dto.DetalleGrupoResponseDTO;
import com.wasimasi.api.dto.GrupoResponseDTO;
import com.wasimasi.api.dto.MensajeGrupoResponseDTO;
import com.wasimasi.api.dto.MensajeRequestDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ConflictException;
import com.wasimasi.api.exception.ForbiddenException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.GrupoMapper;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.MensajeGrupo;
import com.wasimasi.api.model.MiembroGrupo;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.model.Vivienda;
import com.wasimasi.api.repository.MensajeGrupoRepository;
import com.wasimasi.api.repository.MiembroGrupoRepository;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.ViviendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** US15 - Membresía por visita confirmada y chat de una vivienda. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrupoService {
    private final ViviendaRepository viviendas;
    private final MiembroGrupoRepository miembros;
    private final MensajeGrupoRepository mensajes;
    private final PublicacionRepository publicaciones;
    private final GrupoMapper mapper;

    /** US15 - Serializa altas, envíos y cambios de disponibilidad de una vivienda. */
    public Vivienda bloquear(Long id) {
        return viviendas.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la vivienda"));
    }

    /** US15 - Se invoca con la vivienda bloqueada en la transacción de visita o vinculación. */
    public void incorporarVisitaConfirmada(Vivienda vivienda, Usuario buscador) {
        vivienda.iniciarGrupo();
        incorporar(vivienda, vivienda.getAnfitrion());
        incorporar(vivienda, buscador);
        cerrarSiOcupada(vivienda);
    }

    /** US15 - Cierra el grupo cuando hay una habitación ocupada y ya no quedan habitaciones activas. */
    public void cerrarSiOcupada(Vivienda vivienda) {
        if (vivienda.getGrupoCreadoEn() != null && !vivienda.grupoCerrado()
                && !publicaciones.existsByViviendaIdAndEstado(vivienda.getId(), EstadoPublicacion.ACTIVA)
                && publicaciones.existsByViviendaIdAndEstado(vivienda.getId(), EstadoPublicacion.OCUPADA)) {
            vivienda.cerrarGrupo();
        }
    }

    public List<GrupoResponseDTO> listar(Long usuarioId) {
        return miembros.findByUsuarioIdOrderByViviendaGrupoCreadoEnDesc(usuarioId).stream()
                .map(MiembroGrupo::getVivienda)
                .map(mapper::toGrupoDTO)
                .toList();
    }

    public DetalleGrupoResponseDTO obtener(Long usuarioId, Long viviendaId) {
        Vivienda vivienda = buscarMiembro(usuarioId, viviendaId).getVivienda();
        return detalle(vivienda, mensajes.findByViviendaIdOrderByIdAsc(viviendaId));
    }

    public DetalleGrupoResponseDTO listarMensajes(Long usuarioId, Long viviendaId, Long despuesDe) {
        Vivienda vivienda = buscarMiembro(usuarioId, viviendaId).getVivienda();
        if (despuesDe < 0) throw new BadRequestException("El cursor de mensajes debe ser positivo o cero");
        return detalle(vivienda, mensajes.findByViviendaIdAndIdGreaterThanOrderByIdAsc(
                viviendaId, despuesDe, PageRequest.of(0, 100)));
    }

    @Transactional
    public MensajeGrupoResponseDTO enviar(Long usuarioId, Long viviendaId, MensajeRequestDTO request) {
        Vivienda vivienda = bloquear(viviendaId);
        MiembroGrupo miembro = buscarMiembro(usuarioId, viviendaId);
        String contenido = request.contenido().strip();
        return mensajes.findByViviendaIdAndEmisorIdAndClienteId(viviendaId, usuarioId, request.clienteId())
                .map(existente -> {
                    if (!existente.getContenido().equals(contenido)) {
                        throw new ConflictException("El identificador de envío ya corresponde a otro mensaje");
                    }
                    return mapper.toMensajeDTO(existente);
                })
                .orElseGet(() -> {
                    if (vivienda.grupoCerrado()) {
                        throw new ConflictException("El grupo está cerrado");
                    }
                    return mapper.toMensajeDTO(mensajes.save(
                            new MensajeGrupo(vivienda, miembro.getUsuario(), contenido, request.clienteId())));
                });
    }

    @Transactional
    public void marcarLectura(Long usuarioId, Long viviendaId, Long mensajeId) {
        bloquear(viviendaId);
        MiembroGrupo miembro = buscarMiembro(usuarioId, viviendaId);
        if (!mensajes.existsByIdAndViviendaId(mensajeId, viviendaId)) {
            throw new BadRequestException("El mensaje no pertenece a esta conversación");
        }
        miembro.registrarLectura(mensajeId);
    }

    private void incorporar(Vivienda vivienda, Usuario usuario) {
        if (!miembros.existsByViviendaIdAndUsuarioId(vivienda.getId(), usuario.getId())) {
            miembros.save(new MiembroGrupo(vivienda, usuario));
        }
    }

    private MiembroGrupo buscarMiembro(Long usuarioId, Long viviendaId) {
        return miembros.findByViviendaIdAndUsuarioId(viviendaId, usuarioId)
                .orElseThrow(() -> new ForbiddenException("Necesitas una visita confirmada para unirte a este grupo"));
    }

    private DetalleGrupoResponseDTO detalle(Vivienda vivienda, List<MensajeGrupo> historial) {
        return new DetalleGrupoResponseDTO(mapper.toGrupoDTO(vivienda),
                historial.stream().map(mapper::toMensajeDTO).toList());
    }
}
