import { I18nService } from '../i18n/i18n.service';
import { Publicacion } from '../models/publicacion.model';

/** Textos traducidos de las condiciones y coordenadas de una publicación (US09/US10). */

export function textoMascotas(publicacion: Publicacion, i18n: I18nService): string | null {
  const permite = publicacion.mascotasPermitidas;
  return permite == null ? null : i18n.t(permite ? 'buscar.si' : 'buscar.no');
}

export function textoGenero(publicacion: Publicacion, i18n: I18nService): string | null {
  const genero = publicacion.generoPreferido;
  if (!genero) {
    return null;
  }
  if (genero === 'M') {
    return i18n.t('buscar.masculino');
  }
  if (genero === 'F') {
    return i18n.t('buscar.femenino');
  }
  return i18n.t('buscar.cualquier');
}

export function textoServicios(publicacion: Publicacion): string | null {
  return publicacion.servicios?.trim() || null;
}

export function textoCoordenadas(publicacion: Publicacion, i18n: I18nService): string | null {
  const { latitud, longitud } = publicacion;
  if (latitud == null || longitud == null) {
    return null;
  }
  return i18n.t('pub.coordenadas', {
    lat: Number(latitud).toFixed(4),
    lon: Number(longitud).toFixed(4),
  });
}
