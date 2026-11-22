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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reglas de negocio de US06 (publicar), US08 (buscar), US09 (condiciones)
 * y US11 (ver detalle).
 */
@Service
@RequiredArgsConstructor
public class PublicacionService {

    private static final Set<String> GENEROS_VALIDOS = Set.of("M", "F");

    /** US09 - Un único campo agrupa las condiciones que el anfitrión declara rígidas. */
    private static final List<String> CONDICIONES_VALIDAS = List.of("mascotas", "genero", "servicios");

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
                .mascotasPermitidas(request.getMascotasPermitidas())
                .generoPreferido(normalizarGenero(request.getGeneroPreferido()))
                .servicios(request.getServicios())
                .condicionesNoNegociables(serializarCondicionesNoNegociables(request.getCondicionesNoNegociables()))
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .estado(anfitrionVerificado ? EstadoPublicacion.ACTIVA : EstadoPublicacion.BORRADOR)
                .build();

        Publicacion guardada = publicacionRepository.save(publicacion);
        PublicacionResponseDTO response = publicacionMapper.toResponseDTO(guardada);
        response.setCondicionesNegociablesIncumplidas(List.of());

        if (!anfitrionVerificado) {
            response.setAdvertencia("Verifica tu identidad para publicar");
        }
        return response;
    }

    /**
     * US08 + US09 - Busca habitaciones activas por zona y presupuesto. Solo excluye
     * resultados por condiciones NO negociables; las negociables que no coinciden
     * se devuelven igual y se avisa en {@code condicionesNegociablesIncumplidas}.
     */
    public List<PublicacionResponseDTO> buscar(String zona, BigDecimal precioMin, BigDecimal precioMax,
                                               Boolean mascotas, String genero, String servicios) {
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new BadRequestException("El presupuesto minimo no puede ser mayor al maximo");
        }

        String generoNormalizado = normalizarGenero(genero);
        // "Any" significa que al buscador le da igual: no filtra por género.
        final String generoFiltro = "Any".equals(generoNormalizado) ? null : generoNormalizado;
        Set<String> serviciosPedidos = parsearServicios(servicios);

        return publicacionRepository
                .findByEstadoAndZonaIgnoreCaseAndPrecioBetween(EstadoPublicacion.ACTIVA, zona, precioMin, precioMax)
                .stream()
                .filter(p -> {
                    Set<String> rigidas = clavesNoNegociables(p);
                    return (mascotas == null || !esNoNegociable(rigidas, "mascotas")
                            || mascotas.equals(p.getMascotasPermitidas()))
                            && (generoFiltro == null || !esNoNegociable(rigidas, "genero")
                            || coincideGenero(p.getGeneroPreferido(), generoFiltro))
                            && (serviciosPedidos.isEmpty() || !esNoNegociable(rigidas, "servicios")
                            || contieneServicios(p.getServicios(), serviciosPedidos));
                })
                .map(p -> {
                    PublicacionResponseDTO dto = publicacionMapper.toResponseDTO(p);
                    dto.setCondicionesNegociablesIncumplidas(
                            avisosCondiciones(p, mascotas, generoFiltro, serviciosPedidos));
                    return dto;
                })
                .toList();
    }

    public PublicacionResponseDTO verDetalle(Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la publicacion con id " + publicacionId));
        PublicacionResponseDTO response = publicacionMapper.toResponseDTO(publicacion);
        // El detalle no conoce los filtros usados en la búsqueda, así que no lleva avisos.
        response.setCondicionesNegociablesIncumplidas(List.of());
        return response;
    }

    /** Acepta M/F (o Any) sin importar mayúsculas; rejects any other value. */
    private String normalizarGenero(String genero) {
        if (genero == null || genero.isBlank()) {
            return null;
        }
        String texto = genero.trim().toUpperCase(Locale.ROOT);
        if ("ANY".equals(texto)) {
            return "Any";
        }
        if (!GENEROS_VALIDOS.contains(texto)) {
            throw new BadRequestException("El genero debe ser M, F o Any");
        }
        return texto;
    }

    /** Convierte "agua, wifi" en un conjunto de servicios normalizados. */
    private Set<String> parsearServicios(String servicios) {
        if (servicios == null || servicios.isBlank()) {
            return Set.of();
        }
        Set<String> resultado = new LinkedHashSet<>();
        Arrays.stream(servicios.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .forEach(resultado::add);
        return resultado;
    }

    /** La publicación debe incluir TODOS los servicios pedidos. */
    private boolean contieneServicios(String serviciosPublicacion, Set<String> pedidos) {
        return parsearServicios(serviciosPublicacion).containsAll(pedidos);
    }

    /**
     * US09 - Guarda las condiciones rígidas sin repetidos y en orden fijo.
     * Null se conserva para las publicaciones creadas antes de US09.
     */
    private String serializarCondicionesNoNegociables(List<String> condiciones) {
        if (condiciones == null) {
            return null;
        }
        Set<String> normalizadas = new LinkedHashSet<>();
        for (String condicion : condiciones) {
            if (condicion == null || condicion.isBlank()) {
                continue;
            }
            String clave = condicion.trim().toLowerCase(Locale.ROOT);
            if (!CONDICIONES_VALIDAS.contains(clave)) {
                throw new BadRequestException(
                        "La condicion " + condicion + " no es valida. Usa mascotas, genero o servicios");
            }
            normalizadas.add(clave);
        }
        StringBuilder csv = new StringBuilder();
        for (String clave : CONDICIONES_VALIDAS) {
            if (normalizadas.contains(clave)) {
                if (csv.length() > 0) {
                    csv.append(',');
                }
                csv.append(clave);
            }
        }
        return csv.toString();
    }

    /** US09 - Null = publicación anterior a US09: todas las condiciones siguen siendo rígidas. */
    private Set<String> clavesNoNegociables(Publicacion publicacion) {
        String csv = publicacion.getCondicionesNoNegociables();
        if (csv == null) {
            return null;
        }
        Set<String> claves = new LinkedHashSet<>();
        Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(clave -> !clave.isEmpty())
                .map(clave -> clave.toLowerCase(Locale.ROOT))
                .forEach(claves::add);
        return claves;
    }

    private boolean esNoNegociable(Set<String> rigidas, String clave) {
        return rigidas == null || rigidas.contains(clave);
    }

    /** US09 - Claves de condiciones negociables que no coinciden con la búsqueda. */
    private List<String> avisosCondiciones(Publicacion publicacion, Boolean mascotas, String genero,
                                           Set<String> serviciosPedidos) {
        Set<String> rigidas = clavesNoNegociables(publicacion);
        List<String> avisos = new ArrayList<>();
        if (mascotas != null && !esNoNegociable(rigidas, "mascotas")
                && !mascotas.equals(publicacion.getMascotasPermitidas())) {
            avisos.add("mascotas");
        }
        if (genero != null && !esNoNegociable(rigidas, "genero")
                && !coincideGenero(publicacion.getGeneroPreferido(), genero)) {
            avisos.add("genero");
        }
        if (!serviciosPedidos.isEmpty() && !esNoNegociable(rigidas, "servicios")
                && !contieneServicios(publicacion.getServicios(), serviciosPedidos)) {
            avisos.add("servicios");
        }
        return avisos;
    }

    /** US09 - El género "Any" de la publicación es comodín, igual que en el filtro. */
    private boolean coincideGenero(String generoPublicacion, String generoFiltro) {
        return generoFiltro.equals(generoPublicacion) || "Any".equals(generoPublicacion);
    }
}