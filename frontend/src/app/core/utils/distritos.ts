/**
 * Distritos de Lima Metropolitana sugeridos en la búsqueda y al publicar.
 * El backend compara la zona sin distinguir mayúsculas, pero sí tildes;
 * por eso la zona se envía siempre normalizada (ver normalizarZona).
 */
export const DISTRITOS_LIMA: readonly string[] = [
  'Barranco', 'Breña', 'Chorrillos', 'Jesús María', 'La Molina', 'La Victoria', 'Lima', 'Lince',
  'Los Olivos', 'Magdalena del Mar', 'Miraflores', 'Pueblo Libre', 'San Borja', 'San Isidro',
  'San Juan de Lurigancho', 'San Juan de Miraflores', 'San Martín de Porres', 'San Miguel',
  'Santiago de Surco', 'Surquillo',
];

/** Atajos de la pantalla de búsqueda (zonas cercanas a las universidades). */
export const ZONAS_POPULARES: readonly string[] = ['Santiago de Surco', 'San Isidro', 'Miraflores', 'San Miguel', 'Barranco'];

/** Quita las tildes: "Jesús María" -> "Jesus Maria". */
export function sinTildes(texto: string): string {
  return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

/**
 * Zona tal como se guarda y se busca en el backend: sin tildes y sin espacios extra.
 * Así "Jesús María" y "Jesus Maria" encuentran las mismas habitaciones.
 */
export function normalizarZona(zona: string): string {
  return sinTildes(zona).trim().replace(/\s+/g, ' ');
}

/** Filtra distritos por texto ignorando mayúsculas y tildes. */
export function filtrarDistritos(texto: string): string[] {
  const buscado = sinTildes(texto.trim()).toLowerCase();
  return DISTRITOS_LIMA.filter((d) => sinTildes(d).toLowerCase().includes(buscado));
}
