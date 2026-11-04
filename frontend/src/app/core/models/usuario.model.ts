// Espejo de los DTO del backend (paquete com.wasimasi.api.dto / model).

export type RolUsuario = 'BUSCADOR' | 'ANFITRION';

export type EstadoVerificacion = 'PENDIENTE' | 'VERIFICADO' | 'RECHAZADO';

/** UsuarioResponseDTO */
export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: RolUsuario;
  universidad: string | null;
  estadoVerificacion: EstadoVerificacion;
}
