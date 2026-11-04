import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models/usuario.model';
import { AuthService } from './auth.service';

/** Perfil y verificación del usuario (US03, US04, US05). */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly api = environment.apiUrl;

  /** GET /usuarios/me: datos del usuario del token y su estado de verificación. */
  obtenerPerfil(): Observable<Usuario> {
    return this.http
      .get<Usuario>(`${this.api}/usuarios/me`)
      .pipe(tap((usuario) => this.auth.actualizarUsuario(usuario)));
  }

  /** POST /usuarios/{id}/simular-verificacion: verificación simulada (solo la propia cuenta). */
  simularVerificacion(id: number): Observable<Usuario> {
    return this.http
      .post<Usuario>(`${this.api}/usuarios/${id}/simular-verificacion`, {})
      .pipe(tap((usuario) => this.auth.actualizarUsuario(usuario)));
  }
}
