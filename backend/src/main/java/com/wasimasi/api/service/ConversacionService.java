package com.wasimasi.api.service;

import com.wasimasi.api.dto.ContactoRequestDTO;
import com.wasimasi.api.dto.ContactoResponseDTO;
import com.wasimasi.api.dto.ConversacionNoLeidaResponseDTO;
import com.wasimasi.api.dto.ConversacionResponseDTO;
import com.wasimasi.api.dto.DetalleConversacionResponseDTO;
import com.wasimasi.api.dto.MensajeRequestDTO;
import com.wasimasi.api.dto.MensajeResponseDTO;
import com.wasimasi.api.dto.MensajesNoLeidosResponseDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.exception.ConflictException;
import com.wasimasi.api.exception.ForbiddenException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.mapper.ConversacionMapper;
import com.wasimasi.api.model.Conversacion;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.Mensaje;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.ConversacionRepository;
import com.wasimasi.api.repository.MensajeGrupoRepository;
import com.wasimasi.api.repository.MensajeRepository;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/** US12 - Contacto desde una publicación y bandeja de conversaciones. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversacionService {

    private final ConversacionRepository conversaciones;
    private final MensajeRepository mensajes;
    private final MensajeGrupoRepository mensajesGrupo;
    private final PublicacionRepository publicaciones;
    private final UsuarioRepository usuarios;
    private final ConversacionMapper mapper;

    public ContactoResponseDTO consultarContacto(Long buscadorId, Long publicacionId) {
        Publicacion publicacion = buscarPublicacionActiva(publicacionId);
        return conversaciones.findByBuscadorIdAndAnfitrionId(buscadorId, publicacion.getAnfitrion().getId())
                .map(conversacion -> new ContactoResponseDTO(conversacion.getId()))
                .orElseGet(() -> new ContactoResponseDTO(null));
    }

    @Transactional
    public ContactoResponseDTO iniciarContacto(Long buscadorId, Long publicacionId, ContactoRequestDTO request) {
        // Serializa el primer contacto del mismo buscador, incluso si sale de dos anuncios distintos.
        Usuario buscador = usuarios.findByIdForUpdate(buscadorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el buscador"));
        Publicacion publicacion = buscarPublicacionActiva(publicacionId);
        return conversaciones.findByBuscadorIdAndAnfitrionId(buscadorId, publicacion.getAnfitrion().getId())
                .map(conversacion -> new ContactoResponseDTO(conversacion.getId()))
                .orElseGet(() -> {
                    Conversacion conversacion = conversaciones.save(new Conversacion(buscador, publicacion));
                    mensajes.save(new Mensaje(conversacion, buscador, request.mensaje().strip()));
                    return new ContactoResponseDTO(conversacion.getId());
                });
    }

    public List<ConversacionResponseDTO> listar(Long usuarioId) {
        return conversaciones.findByBuscadorIdOrAnfitrionIdOrderByIdDesc(usuarioId, usuarioId).stream()
                .map(conversacion -> mapper.toResponseDTO(conversacion, obtenerInterlocutor(conversacion, usuarioId)))
                .toList();
    }

    public DetalleConversacionResponseDTO obtener(Long usuarioId, Long conversacionId) {
        Conversacion conversacion = buscarConversacion(conversacionId);
        Usuario interlocutor = obtenerInterlocutor(conversacion, usuarioId);
        List<MensajeResponseDTO> historial = mensajes.findByConversacionIdOrderByIdAsc(conversacionId).stream()
                .map(mapper::toResponseDTO)
                .toList();
        return new DetalleConversacionResponseDTO(mapper.toResponseDTO(conversacion, interlocutor), historial);
    }

    /** US13 - Recupera el siguiente lote sin volver a descargar el historial. */
    public List<MensajeResponseDTO> listarMensajes(Long usuarioId, Long conversacionId, Long despuesDe) {
        verificarParticipante(usuarioId, buscarConversacion(conversacionId));
        if (despuesDe < 0) {
            throw new BadRequestException("El cursor de mensajes debe ser positivo o cero");
        }
        return mensajes.findByConversacionIdAndIdGreaterThanOrderByIdAsc(conversacionId, despuesDe,
                        PageRequest.of(0, 100)).stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    /** US13 - Persiste el envío una sola vez y serializa la asignación de IDs por conversación. */
    @Transactional
    public MensajeResponseDTO enviar(Long usuarioId, Long conversacionId, MensajeRequestDTO request) {
        Conversacion conversacion = bloquearConversacion(conversacionId);
        verificarParticipante(usuarioId, conversacion);
        String contenido = request.contenido().strip();
        Mensaje mensaje = mensajes.findByConversacionIdAndEmisorIdAndClienteId(conversacionId, usuarioId,
                        request.clienteId())
                .map(existente -> {
                    if (!existente.getContenido().equals(contenido)) {
                        throw new ConflictException("El identificador de envío ya corresponde a otro mensaje");
                    }
                    return existente;
                })
                .orElseGet(() -> mensajes.save(new Mensaje(conversacion, usuarios.getReferenceById(usuarioId),
                        contenido, request.clienteId())));
        return mapper.toResponseDTO(mensaje);
    }

    /** US13 - Confirma únicamente mensajes que pertenecen a la conversación. */
    @Transactional
    public void marcarLectura(Long usuarioId, Long conversacionId, Long mensajeId) {
        Conversacion conversacion = bloquearConversacion(conversacionId);
        verificarParticipante(usuarioId, conversacion);
        if (!mensajes.existsByIdAndConversacionId(mensajeId, conversacionId)) {
            throw new BadRequestException("El mensaje no pertenece a esta conversación");
        }
        conversacion.registrarLectura(usuarioId, mensajeId);
    }

    /** US14 - Usa el mismo cursor de lectura del chat para obtener el contador del usuario. */
    public MensajesNoLeidosResponseDTO consultarNoLeidos(Long usuarioId) {
        List<ConversacionNoLeidaResponseDTO> pendientes = Stream.concat(
                mensajes.resumirNoLeidos(usuarioId).stream(), mensajesGrupo.resumirNoLeidos(usuarioId).stream()).toList();
        long total = pendientes.stream().mapToLong(ConversacionNoLeidaResponseDTO::cantidad).sum();
        return new MensajesNoLeidosResponseDTO(total > 0, total, pendientes);
    }

    private Conversacion buscarConversacion(Long conversacionId) {
        return conversaciones.findById(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la conversación"));
    }

    private Conversacion bloquearConversacion(Long conversacionId) {
        return conversaciones.findByIdForUpdate(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la conversación"));
    }

    private void verificarParticipante(Long usuarioId, Conversacion conversacion) {
        obtenerInterlocutor(conversacion, usuarioId);
    }

    private Publicacion buscarPublicacionActiva(Long publicacionId) {
        Publicacion publicacion = publicaciones.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación"));
        if (publicacion.getEstado() != EstadoPublicacion.ACTIVA) {
            throw new ConflictException("Solo puedes contactar al anfitrión desde una publicación activa");
        }
        return publicacion;
    }

    private Usuario obtenerInterlocutor(Conversacion conversacion, Long usuarioId) {
        if (conversacion.getBuscador().getId().equals(usuarioId)) {
            return conversacion.getAnfitrion();
        }
        if (conversacion.getAnfitrion().getId().equals(usuarioId)) {
            return conversacion.getBuscador();
        }
        throw new ForbiddenException("Solo los participantes pueden acceder a esta conversación");
    }
}
