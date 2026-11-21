export type EstadoPublicacion = 'BORRADOR' | 'ACTIVA' | 'PAUSADA' | 'OCUPADA';

/** PublicacionRequestDTO (POST /publicaciones). El anfitrión sale del JWT. */
export interface PublicacionRequest {
  zona: string;
  precio: number;
  descripcion: string;
  fotos: string[];
  /** US09 - Condiciones negociables/no negociables. */
  mascotasPermitidas?: boolean;
  generoPreferido?: 'M' | 'F' | 'Any';
  servicios?: string; // Coma-separado: "agua,electricidad,wifi"
  /** US10 - Coordenadas para el mapa (opcionales, van juntas). */
  latitud?: number;
  longitud?: number;
}

/** PublicacionResponseDTO */
export interface Publicacion {
  id: number;
  anfitrionId: number;
  anfitrionNombre: string;
  anfitrionVerificado: boolean;
  zona: string;
  precio: number;
  descripcion: string;
  fotos: string[];
  estado: EstadoPublicacion;
  fechaPublicacion: string;
  /** Solo llega cuando el anfitrión no está verificado y la publicación queda en BORRADOR. */
  advertencia: string | null;
  /** US09 - Condiciones negociables/no negociables. */
  mascotasPermitidas?: boolean;
  generoPreferido?: 'M' | 'F' | 'Any';
  servicios?: string; // Coma-separado: "agua,electricidad,wifi"
  /** US10 - Coordenadas del mapa. */
  latitud?: number;
  longitud?: number;
}

/** Filtros de GET /publicaciones (los tres son obligatorios, los de US09 son opcionales). */
export interface FiltrosBusqueda {
  zona: string;
  precioMin: number;
  precioMax: number;
  /** US09 - Filtros opcionales; si no llegan, el backend no filtra por ellos. */
  mascotas?: boolean;
  genero?: 'M' | 'F' | 'Any';
  servicios?: string;
}
