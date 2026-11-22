export type EstadoPublicacion = 'BORRADOR' | 'ACTIVA' | 'PAUSADA' | 'OCUPADA';
export type EstadoGestionPublicacion = Exclude<EstadoPublicacion, 'BORRADOR'>;

/** PublicacionRequestDTO (POST /publicaciones). El anfitrión sale del JWT. */
export interface PublicacionRequest {
  zona: string;
  precio: number;
  descripcion: string;
  fotos: string[];
}

/** EditarPublicacionRequestDTO (PUT /publicaciones/{id}). */
export interface EditarPublicacionRequest extends PublicacionRequest {
  titulo: string | null;
}

/** PublicacionResponseDTO */
export interface Publicacion {
  id: number;
  anfitrionId: number;
  viviendaId?: number;
  anfitrionNombre: string;
  anfitrionVerificado: boolean;
  titulo?: string | null;
  zona: string;
  precio: number;
  descripcion: string | null;
  fotos: string[];
  estado: EstadoPublicacion;
  fechaPublicacion: string;
  /** Solo llega cuando el anfitrión no está verificado y la publicación queda en BORRADOR. */
  advertencia: string | null;
}

export interface Vivienda {
  id: number;
  nombre: string;
}

/** Filtros de GET /publicaciones (los tres son obligatorios en el backend). */
export interface FiltrosBusqueda {
  zona: string;
  precioMin: number;
  precioMax: number;
}
