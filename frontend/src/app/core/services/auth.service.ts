import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RegistroRequest } from '../models/auth.model';
import { Usuario } from '../models/usuario.model';

const TOKEN_KEY = 'wm_token';
const USUARIO_KEY = 'wm_usuario';

/**
 * Sesión del usuario (US01 registro, US02 login).
 * Guarda el JWT que devuelve POST /auth/login; el interceptor lo envía en cada petición.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly api = environment.apiUrl;

  private readonly _token = signal<string | null>(leer(TOKEN_KEY));
  private readonly _usuario = signal<Usuario | null>(leerJson<Usuario>(USUARIO_KEY));

  readonly token = this._token.asReadonly();
  readonly usuario = this._usuario.asReadonly();
  readonly isLoggedIn = computed(() => {
    const token = this._token();
    return token !== null && !tokenExpirado(token);
  });
  readonly esAnfitrion = computed(() => this._usuario()?.rol === 'ANFITRION');
  readonly verificado = computed(() => this._usuario()?.estadoVerificacion === 'VERIFICADO');

  registrar(datos: RegistroRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.api}/auth/registro`, datos);
  }

  login(credenciales: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.api}/auth/login`, credenciales)
      .pipe(tap((res) => this.guardarSesion(res.token, res.usuario)));
  }

  /** Actualiza los datos del usuario en sesión (por ejemplo, después de verificarse). */
  actualizarUsuario(usuario: Usuario): void {
    this._usuario.set(usuario);
    escribir(USUARIO_KEY, JSON.stringify(usuario));
  }

  logout(redirigir = true): void {
    this._token.set(null);
    this._usuario.set(null);
    borrar(TOKEN_KEY);
    borrar(USUARIO_KEY);
    if (redirigir) {
      this.router.navigate(['/login']);
    }
  }

  private guardarSesion(token: string, usuario: Usuario): void {
    this._token.set(token);
    escribir(TOKEN_KEY, token);
    this.actualizarUsuario(usuario);
  }
}

/** Lee el claim "exp" del JWT (en segundos) para saber si ya venció. */
function tokenExpirado(token: string): boolean {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const { exp } = JSON.parse(atob(payload)) as { exp?: number };
    return exp !== undefined && exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

// localStorage puede no estar disponible (modo privado): nunca debe romper la app.
function leer(key: string): string | null {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}
function leerJson<T>(key: string): T | null {
  const valor = leer(key);
  try {
    return valor ? (JSON.parse(valor) as T) : null;
  } catch {
    return null;
  }
}
function escribir(key: string, valor: string): void {
  try {
    localStorage.setItem(key, valor);
  } catch {
    /* sin almacenamiento */
  }
}
function borrar(key: string): void {
  try {
    localStorage.removeItem(key);
  } catch {
    /* sin almacenamiento */
  }
}
