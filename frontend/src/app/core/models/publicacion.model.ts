export type EstadoPublicacion = 'BORRADOR' | 'ACTIVA' | 'PAUSADA' | 'OCUPADA';

/** PublicacionRequestDTO (POST /publicaciones). El anfitrión sale del JWT. */
export interface PublicacionRequest {
  zona: string;
  precio: number;
  descripcion: string;
  fotos: string[];
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
}

/** Filtros de GET /publicaciones (los tres son obligatorios en el backend). */
export interface FiltrosBusqueda {
  zona: string;
  precioMin: number;
  precioMax: number;
}
