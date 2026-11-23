export const INTERVALO_CHAT_MS = 2000;

export interface ContactoResponse {
  conversacionId: number | null;
}

export interface ConversacionResumen {
  id: number;
  interlocutorNombre: string;
  fechaCreacion: string;
  grupal?: boolean;
  cerrada?: boolean;
}

export interface Mensaje {
  id: number;
  emisorId: number;
  emisorNombre?: string;
  contenido: string;
  fechaEnvio: string;
  clienteId: string | null;
}

export interface MensajePendiente {
  clienteId: string;
  conversacionId: number;
  grupal: boolean;
  usuarioId: number;
  contenido: string;
  estado: 'enviando' | 'no-enviado';
  reintentar: boolean;
  error?: string;
}

export interface DetalleConversacion {
  conversacion: ConversacionResumen;
  mensajes: Mensaje[];
}

export interface MensajesNoLeidos {
  disponibles: boolean;
  total: number;
  conversaciones: {
    conversacionId: number;
    cantidad: number;
    ultimoMensajeId: number;
    grupal: boolean;
  }[];
}
