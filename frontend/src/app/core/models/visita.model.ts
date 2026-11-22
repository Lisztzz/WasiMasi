export type EstadoVisita = 'PENDIENTE' | 'CONFIRMADA' | 'RECHAZADA' | 'CANCELADA';
export type RespuestaVisita = 'CONFIRMADA' | 'RECHAZADA';

/** Datos para solicitar una visita; el buscador se obtiene de la sesión JWT. */
export interface VisitaRequest {
  publicacionId: number;
  fechaHora: string;
  comentario: string | null;
}

/** Solicitud de visita devuelta por la API. */
export interface Visita {
  id: number;
  publicacionId: number;
  zona: string;
  buscadorId: number;
  buscadorNombre: string;
  anfitrionNombre: string;
  fechaHora: string;
  estado: EstadoVisita;
  comentario: string | null;
  motivoRespuesta: string | null;
  creadaEn: string;
  actualizadaEn: string | null;
}

export interface VisitaRespuestaRequest {
  estado: RespuestaVisita;
  motivoRespuesta?: string;
}

/** Comparación de personas interesadas a partir de solicitudes reales de visita. */
export interface Interesado {
  visitaId: number;
  buscadorId: number;
  nombre: string;
  universidad: string | null;
  estadoVerificacion: 'PENDIENTE' | 'VERIFICADO' | 'RECHAZADO';
  fechaRegistro: string;
  fechaSolicitud: string;
  fechaHoraVisita: string;
  estadoVisita: EstadoVisita;
  comentario: string | null;
}
