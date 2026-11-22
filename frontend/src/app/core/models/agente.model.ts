import { Publicacion } from './publicacion.model';

/** US21 - Turno anterior que se envía como contexto al agente. */
export interface MensajeAgente {
  rol: 'USUARIO' | 'ASISTENTE';
  contenido: string;
}

/** ConsultaAgenteRequestDTO (POST /agente/consultas). */
export interface ConsultaAgente {
  mensaje: string;
  historial?: MensajeAgente[];
}

export type EstadoAccionAgente = 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA' | 'EXPIRADA';

/** US22 - Acción que el agente propone y el usuario confirma o cancela. */
export interface AccionAgente {
  id: number;
  tipo: 'SOLICITAR_VISITA';
  estado: EstadoAccionAgente;
  publicacionId: number;
  zona: string | null;
  fechaHora: string;
  comentario: string | null;
  visitaId: number | null;
}

/** RespuestaAgenteDTO. */
export interface RespuestaAgente {
  respuesta: string;
  publicaciones: Publicacion[];
  accionPendiente: AccionAgente | null;
}
