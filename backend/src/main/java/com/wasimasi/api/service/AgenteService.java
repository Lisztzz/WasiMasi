package com.wasimasi.api.service;

import com.wasimasi.api.dto.AccionAgenteDTO;
import com.wasimasi.api.dto.ConsultaAgenteRequestDTO;
import com.wasimasi.api.dto.MensajeAgenteDTO;
import com.wasimasi.api.dto.RespuestaAgenteDTO;
import com.wasimasi.api.dto.VisitaRequestDTO;
import com.wasimasi.api.dto.VisitaResponseDTO;
import com.wasimasi.api.exception.ConflictException;
import com.wasimasi.api.exception.ResourceNotFoundException;
import com.wasimasi.api.exception.ServicioNoDisponibleException;
import com.wasimasi.api.model.AccionAgente;
import com.wasimasi.api.model.EstadoAccionAgente;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.AccionAgenteRepository;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * US21 - Agente de IA que responde en lenguaje natural usando datos reales de WasiMasi.
 * US22 - Las acciones que propone (solicitar una visita) solo se ejecutan cuando el usuario confirma.
 */
@Service
public class AgenteService {

    private static final Logger log = LoggerFactory.getLogger(AgenteService.class);
    static final String CLAVE_SIN_CONFIGURAR = "sin-configurar";
    static final Duration VIGENCIA_PROPUESTA = Duration.ofMinutes(30);
    static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final ChatClient chatClient;
    private final PublicacionService publicacionService;
    private final VisitaService visitaService;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final AccionAgenteRepository accionRepository;
    private final String apiKey;
    private final Clock clock;

    @Autowired
    public AgenteService(ChatClient.Builder chatClientBuilder,
                         PublicacionService publicacionService,
                         VisitaService visitaService,
                         UsuarioRepository usuarioRepository,
                         PublicacionRepository publicacionRepository,
                         AccionAgenteRepository accionRepository,
                         @Value("${spring.ai.openai.api-key:" + CLAVE_SIN_CONFIGURAR + "}") String apiKey) {
        this(chatClientBuilder.build(), publicacionService, visitaService, usuarioRepository,
                publicacionRepository, accionRepository, apiKey, Clock.system(ZONA_LIMA));
    }

    AgenteService(ChatClient chatClient, PublicacionService publicacionService, VisitaService visitaService,
                  UsuarioRepository usuarioRepository, PublicacionRepository publicacionRepository,
                  AccionAgenteRepository accionRepository, String apiKey, Clock clock) {
        this.chatClient = chatClient;
        this.publicacionService = publicacionService;
        this.visitaService = visitaService;
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.accionRepository = accionRepository;
        this.apiKey = apiKey;
        this.clock = clock;
    }

    /** US21 - Responde una consulta. Las habitaciones que devuelve son las que el agente leyó de la base. */
    public RespuestaAgenteDTO consultar(Long usuarioId, ConsultaAgenteRequestDTO request) {
        if (apiKey == null || apiKey.isBlank() || CLAVE_SIN_CONFIGURAR.equals(apiKey)) {
            throw new ServicioNoDisponibleException(
                    "El asistente de IA no está configurado. Define la variable de entorno GROQ_API_KEY en el backend.");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario"));
        HerramientasAgente herramientas =
                new HerramientasAgente(usuario, publicacionService, publicacionRepository, accionRepository);

        String respuesta;
        try {
            respuesta = chatClient.prompt()
                    .system(instrucciones(usuario))
                    .messages(historial(request.historial()))
                    .user(request.mensaje().trim())
                    .tools(herramientas)
                    .call()
                    .content();
        } catch (RuntimeException ex) {
            log.warn("Fallo la consulta al modelo de IA: {}", ex.getMessage());
            throw new ServicioNoDisponibleException(
                    "El asistente no pudo responder en este momento. Inténtalo de nuevo en unos segundos.");
        }

        AccionAgente propuesta = herramientas.accionPropuesta();
        return new RespuestaAgenteDTO(
                respuesta == null ? "" : respuesta.trim(),
                herramientas.publicacionesEncontradas(),
                propuesta == null ? null : aDto(propuesta));
    }

    /** US22 - El usuario confirma la acción propuesta: recién aquí se crea la solicitud de visita (US16). */
    @Transactional
    public AccionAgenteDTO confirmar(Long usuarioId, Long accionId) {
        AccionAgente accion = obtenerPendiente(usuarioId, accionId);
        VisitaRequestDTO solicitud = new VisitaRequestDTO();
        solicitud.setPublicacionId(accion.getPublicacionId());
        solicitud.setFechaHora(accion.getFechaHora());
        solicitud.setComentario(accion.getComentario());
        VisitaResponseDTO visita = visitaService.solicitar(usuarioId, solicitud);

        accion.setEstado(EstadoAccionAgente.CONFIRMADA);
        accion.setVisitaId(visita.getId());
        accion.setResueltaEn(LocalDateTime.now(clock));
        return aDto(accion);
    }

    /** US22 - El usuario rechaza la propuesta: no se ejecuta nada. */
    @Transactional
    public AccionAgenteDTO cancelar(Long usuarioId, Long accionId) {
        AccionAgente accion = obtenerPendiente(usuarioId, accionId);
        accion.setEstado(EstadoAccionAgente.CANCELADA);
        accion.setResueltaEn(LocalDateTime.now(clock));
        return aDto(accion);
    }

    private AccionAgente obtenerPendiente(Long usuarioId, Long accionId) {
        AccionAgente accion = accionRepository.findByIdAndUsuarioId(accionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la acción propuesta"));
        if (accion.getEstado() != EstadoAccionAgente.PENDIENTE) {
            throw new ConflictException("Esta propuesta ya fue " + accion.getEstado().name().toLowerCase(Locale.ROOT));
        }
        if (accion.getCreadaEn().plus(VIGENCIA_PROPUESTA).isBefore(LocalDateTime.now(clock))) {
            accion.setEstado(EstadoAccionAgente.EXPIRADA);
            accion.setResueltaEn(LocalDateTime.now(clock));
            accionRepository.save(accion);
            throw new ConflictException("La propuesta venció. Pídele al asistente que la proponga de nuevo.");
        }
        return accion;
    }

    private AccionAgenteDTO aDto(AccionAgente a) {
        String zona = publicacionRepository.findById(a.getPublicacionId()).map(p -> p.getZona()).orElse(null);
        return new AccionAgenteDTO(a.getId(), a.getTipo(), a.getEstado(), a.getPublicacionId(), zona,
                a.getFechaHora(), a.getComentario(), a.getVisitaId());
    }

    private static List<Message> historial(List<MensajeAgenteDTO> mensajes) {
        if (mensajes == null) {
            return List.of();
        }
        return mensajes.stream()
                .<Message>map(m -> "ASISTENTE".equals(m.rol())
                        ? new AssistantMessage(m.contenido())
                        : new UserMessage(m.contenido()))
                .toList();
    }

    String instrucciones(Usuario usuario) {
        String ahora = LocalDateTime.now(clock)
                .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy, HH:mm", Locale.of("es", "PE")));
        return """
                Eres Wasi, el asistente de WasiMasi, una plataforma de vivienda compartida para estudiantes
                universitarios en Lima, Perú. Hoy es %s (hora de Lima). Hablas con %s, cuyo rol es %s.

                Reglas:
                1. Solo ayudas a buscar habitaciones publicadas en WasiMasi y a coordinar visitas. Si te piden otra
                   cosa, explica amablemente que solo puedes ayudar con eso.
                2. Nunca inventes habitaciones, precios, anfitriones ni disponibilidad. Para hablar de habitaciones
                   llama siempre a la herramienta buscarHabitaciones (o verHabitacion si te dan un id).
                3. Usa el nombre oficial del distrito: Barranco, Brena, Chorrillos, Jesus Maria, La Molina,
                   La Victoria, Lima, Lince, Los Olivos, Magdalena del Mar, Miraflores, Pueblo Libre, San Borja,
                   San Isidro, San Juan de Lurigancho, San Juan de Miraflores, San Martin de Porres, San Miguel,
                   Santiago de Surco, Surquillo. Si no te dicen el distrito, pregúntalo.
                4. Si no dicen presupuesto, busca entre 0 y 3000 soles y menciónalo.
                5. Para una visita necesitas el id de la habitación y una fecha y hora futuras: llama a
                   proponerVisita. Nunca digas que la visita ya está agendada: di que se mostró una propuesta
                   y que debe tocar "Confirmar" para enviarla al anfitrión.
                6. La app ya muestra las habitaciones como tarjetas: no copies todos sus datos; resume en
                   máximo 4 líneas y destaca lo que el usuario pidió. Si alguna condición negociable no coincide,
                   avísalo.
                7. Responde en el mismo idioma del usuario (español o inglés), con un tono cercano y claro.
                """.formatted(ahora, usuario.getNombre(), usuario.getRol());
    }
}
