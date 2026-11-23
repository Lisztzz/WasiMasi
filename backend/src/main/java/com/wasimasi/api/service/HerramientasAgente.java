package com.wasimasi.api.service;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.exception.BadRequestException;
import com.wasimasi.api.model.AccionAgente;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.Publicacion;
import com.wasimasi.api.model.RolUsuario;
import com.wasimasi.api.model.TipoAccionAgente;
import com.wasimasi.api.model.Usuario;
import com.wasimasi.api.repository.AccionAgenteRepository;
import com.wasimasi.api.repository.PublicacionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * US21/US22 - Herramientas que el modelo de IA puede llamar (tool calling de Spring AI).
 *
 * Se crea una instancia por consulta con el usuario autenticado: el modelo nunca
 * decide en nombre de quién actúa. Las herramientas solo LEEN datos reales o
 * PROPONEN acciones; ninguna crea una visita (eso ocurre al confirmar, US22).
 */
public class HerramientasAgente {

    /** Máximo de habitaciones que se devuelven al modelo y a la pantalla por búsqueda. */
    static final int MAX_RESULTADOS = 6;

    private static final Map<String, String> ALIAS_ZONAS = Map.of(
            "surco", "Santiago de Surco",
            "sjl", "San Juan de Lurigancho",
            "sjm", "San Juan de Miraflores",
            "smp", "San Martin de Porres",
            "magdalena", "Magdalena del Mar",
            "cercado", "Lima",
            "cercado de lima", "Lima");

    private final Usuario usuario;
    private final PublicacionService publicacionService;
    private final PublicacionRepository publicacionRepository;
    private final AccionAgenteRepository accionRepository;

    private final Map<Long, PublicacionResponseDTO> encontradas = new LinkedHashMap<>();
    private AccionAgente accionPropuesta;

    public HerramientasAgente(Usuario usuario, PublicacionService publicacionService,
                              PublicacionRepository publicacionRepository, AccionAgenteRepository accionRepository) {
        this.usuario = usuario;
        this.publicacionService = publicacionService;
        this.publicacionRepository = publicacionRepository;
        this.accionRepository = accionRepository;
    }

    @Tool(description = """
            Busca habitaciones ACTIVAS publicadas en WasiMasi en un distrito de Lima y dentro de un presupuesto
            mensual en soles. Devuelve la lista real de habitaciones (id, zona, precio, descripcion).
            Usala siempre antes de hablar de habitaciones concretas: nunca inventes resultados.""")
    public List<Map<String, Object>> buscarHabitaciones(
            @ToolParam(description = "Distrito de Lima, por ejemplo 'Santiago de Surco', 'Miraflores' o 'Barranco'") String zona,
            @ToolParam(description = "Precio minimo mensual en soles; usa 0 si el usuario no lo dice") BigDecimal precioMin,
            @ToolParam(description = "Precio maximo mensual en soles; usa 3000 si el usuario no lo dice") BigDecimal precioMax,
            @ToolParam(required = false, description = "true si el usuario necesita que acepten mascotas; omitir si no lo menciona") Boolean mascotas,
            @ToolParam(required = false, description = "Genero preferido del companero de vivienda: M, F o Any; omitir si no lo menciona") String genero,
            @ToolParam(required = false, description = "Servicios requeridos separados por coma, por ejemplo 'wifi,agua'; omitir si no los menciona") String servicios) {
        String zonaNormalizada = normalizarZona(zona);
        List<PublicacionResponseDTO> resultado = publicacionService.buscar(
                zonaNormalizada, precioMin, precioMax, mascotas, genero, servicios);
        List<Map<String, Object>> resumen = new ArrayList<>();
        for (PublicacionResponseDTO p : resultado.stream().limit(MAX_RESULTADOS).toList()) {
            encontradas.put(p.getId(), p);
            resumen.add(resumir(p));
        }
        if (resumen.isEmpty()) {
            return List.of(Map.of("mensaje", "No hay habitaciones activas en " + zonaNormalizada
                    + " entre S/ " + precioMin + " y S/ " + precioMax + "."));
        }
        return resumen;
    }

    @Tool(description = "Obtiene el detalle real de una habitacion por su id (descripcion, precio, condiciones y anfitrion).")
    public Map<String, Object> verHabitacion(
            @ToolParam(description = "Id de la habitacion, tal como aparece en los resultados de buscarHabitaciones") Long publicacionId) {
        PublicacionResponseDTO p = publicacionService.verDetalle(publicacionId);
        if (p.getEstado() == EstadoPublicacion.ACTIVA) {
            encontradas.put(p.getId(), p);
        }
        Map<String, Object> detalle = resumir(p);
        detalle.put("estado", p.getEstado());
        detalle.put("anfitrion", p.getAnfitrionNombre());
        detalle.put("mascotasPermitidas", p.getMascotasPermitidas());
        detalle.put("generoPreferido", p.getGeneroPreferido());
        detalle.put("servicios", p.getServicios());
        return detalle;
    }

    @Tool(description = """
            PROPONE al usuario solicitar una visita a una habitacion. NO agenda nada: la app le muestra al usuario
            un boton para confirmar o cancelar, y la visita solo se crea si el usuario confirma.
            Necesitas el id de la habitacion y una fecha y hora futura.""")
    public String proponerVisita(
            @ToolParam(description = "Id de la habitacion") Long publicacionId,
            @ToolParam(description = "Fecha y hora de la visita en formato ISO yyyy-MM-ddTHH:mm, hora de Lima") String fechaHora,
            @ToolParam(required = false, description = "Comentario opcional para el anfitrion") String comentario) {
        if (usuario.getRol() != RolUsuario.BUSCADOR) {
            return "No se puede proponer: solo los estudiantes que buscan habitacion pueden solicitar visitas.";
        }
        Publicacion publicacion = publicacionRepository.findById(publicacionId).orElse(null);
        if (publicacion == null || publicacion.getEstado() != EstadoPublicacion.ACTIVA) {
            return "No se puede proponer: la habitacion " + publicacionId + " no existe o ya no esta disponible.";
        }
        LocalDateTime cuando;
        try {
            cuando = LocalDateTime.parse(fechaHora.length() == 16 ? fechaHora : fechaHora.substring(0, Math.min(16, fechaHora.length())));
        } catch (DateTimeParseException | NullPointerException ex) {
            return "No se puede proponer: la fecha debe tener el formato yyyy-MM-ddTHH:mm.";
        }
        if (!cuando.isAfter(LocalDateTime.now())) {
            return "No se puede proponer: la fecha de la visita debe ser futura.";
        }
        String nota = comentario == null || comentario.isBlank() ? null
                : comentario.trim().substring(0, Math.min(500, comentario.trim().length()));
        accionPropuesta = accionRepository.save(AccionAgente.builder()
                .usuario(usuario)
                .tipo(TipoAccionAgente.SOLICITAR_VISITA)
                .publicacionId(publicacionId)
                .fechaHora(cuando)
                .comentario(nota)
                .build());
        return "Propuesta creada (accion " + accionPropuesta.getId() + "). Todavia NO esta agendada: "
                + "el usuario debe confirmarla con el boton 'Confirmar' de la app.";
    }

    /** Habitaciones reales que el agente consultó en esta conversación, en el orden en que aparecieron. */
    public List<PublicacionResponseDTO> publicacionesEncontradas() {
        return List.copyOf(encontradas.values());
    }

    /** Acción que el agente dejó pendiente de confirmación en esta consulta (o null). */
    public AccionAgente accionPropuesta() {
        return accionPropuesta;
    }

    private static Map<String, Object> resumir(PublicacionResponseDTO p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("zona", p.getZona());
        m.put("precioMensual", p.getPrecio());
        if (p.getTitulo() != null) {
            m.put("titulo", p.getTitulo());
        }
        String descripcion = p.getDescripcion() == null ? "" : p.getDescripcion();
        m.put("descripcion", descripcion.length() > 160 ? descripcion.substring(0, 160) + "…" : descripcion);
        m.put("anfitrionVerificado", p.isAnfitrionVerificado());
        if (p.getCondicionesNegociablesIncumplidas() != null && !p.getCondicionesNegociablesIncumplidas().isEmpty()) {
            m.put("condicionesNegociablesQueNoCoinciden", p.getCondicionesNegociablesIncumplidas());
        }
        return m;
    }

    /** Igual que la Web App: sin tildes ni espacios extra, más algunos nombres cortos de uso común. */
    static String normalizarZona(String zona) {
        if (zona == null || zona.isBlank()) {
            throw new BadRequestException("Indica una zona para buscar");
        }
        String sinTildes = Normalizer.normalize(zona, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .replaceAll("\\s+", " ");
        return ALIAS_ZONAS.getOrDefault(sinTildes.toLowerCase(), sinTildes);
    }
}
