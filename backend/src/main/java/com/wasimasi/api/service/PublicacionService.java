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
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .estado(anfitrionVerificado ? EstadoPublicacion.ACTIVA : EstadoPublicacion.BORRADOR)
                .build();

        Publicacion guardada = publicacionRepository.save(publicacion);
        PublicacionResponseDTO response = publicacionMapper.toResponseDTO(guardada);

        if (!anfitrionVerificado) {
            response.setAdvertencia("Verifica tu identidad para publicar");
        }
        return response;
    }

    /**
     * US08 + US09 - Busca habitaciones activas por zona y presupuesto y aplica,
     * cuando llegan, los filtros de condiciones negociables/no negociables.
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
                .filter(p -> mascotas == null || mascotas.equals(p.getMascotasPermitidas()))
                .filter(p -> generoFiltro == null
                        || generoFiltro.equals(p.getGeneroPreferido())
                        || "Any".equals(p.getGeneroPreferido()))
                .filter(p -> serviciosPedidos.isEmpty() || contieneServicios(p.getServicios(), serviciosPedidos))
                .map(publicacionMapper::toResponseDTO)
                .toList();
    }

    public PublicacionResponseDTO verDetalle(Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la publicacion con id " + publicacionId));
        return publicacionMapper.toResponseDTO(publicacion);
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
}