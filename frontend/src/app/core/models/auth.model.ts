import { RolUsuario, Usuario } from './usuario.model';

/** RegistroRequestDTO (POST /auth/registro) */
export interface RegistroRequest {
  nombre: string;
  correo: string;
  password: string;
  rol: RolUsuario;
  codigoEstudiante?: string | null;
  universidad?: string | null;
  documentoIdentidad?: string | null;
}

/** LoginRequestDTO (POST /auth/login) */
export interface LoginRequest {
  correo: string;
  password: string;
}

/** LoginResponseDTO */
export interface LoginResponse {
  token: string;
  tipoToken: string;
  expiraEnSegundos: number;
  usuario: Usuario;
  requiereVerificacion: boolean;
}
