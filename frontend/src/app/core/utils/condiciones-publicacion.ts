import { I18nService } from '../i18n/i18n.service';
import { ClaveCondicion, Publicacion } from '../models/publicacion.model';

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

/** US09 - Una condición es negociable cuando NO está en la lista de condiciones fijas. */
export function esCondicionNegociable(publicacion: Publicacion, clave: ClaveCondicion): boolean {
  const fijas = publicacion.condicionesNoNegociables;
  // null = publicación creada antes de US09: todas las condiciones se filtraban.
  return fijas != null && !fijas.includes(clave);
}

/** US09 - Textos de aviso para las condiciones negociables que no coincidieron en la búsqueda. */
export function avisosCondiciones(publicacion: Publicacion, i18n: I18nService): string[] {
  return (publicacion.condicionesNegociablesIncumplidas ?? [])
    .map((clave) => i18n.t(`buscar.aviso.${clave}`));
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
