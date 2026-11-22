import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginResponse } from '../models/auth.model';
import { Usuario } from '../models/usuario.model';
import { AuthService } from './auth.service';

/** US01/US02 - Registro, login y sesión guardada en el navegador. */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: { navigate: ReturnType<typeof vi.fn> };
  const api = environment.apiUrl;
  const usuario: Usuario = {
    id: 1,
    nombre: 'Ana Pérez',
    correo: 'ana@upc.edu.pe',
    rol: 'ANFITRION',
    universidad: 'UPC',
    estadoVerificacion: 'VERIFICADO',
  };

  function token(expirado = false): string {
    const exp = Math.floor(Date.now() / 1000) + (expirado ? -60 : 3600);
    return `cabecera.${btoa(JSON.stringify({ exp }))}.firma`;
  }

  function respuestaLogin(jwt = token()): LoginResponse {
    return {
      token: jwt,
      tipoToken: 'Bearer',
      expiraEnSegundos: 3600,
      usuario,
      requiereVerificacion: false,
    };
  }

  beforeEach(() => {
    localStorage.clear();
    router = { navigate: vi.fn() };
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('registra al usuario con POST /auth/registro (US01)', () => {
    const datos = {
      nombre: 'Ana Pérez', correo: 'ana@upc.edu.pe', password: 'clave12345', rol: 'ANFITRION' as const,
    };

    service.registrar(datos).subscribe((creado) => expect(creado).toEqual(usuario));

    const req = httpMock.expectOne(`${api}/auth/registro`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(datos);
    req.flush(usuario);
  });

  it('guarda la sesión del login y deriva los estados del usuario (US02)', () => {
    const jwt = token();

    service.login({ correo: usuario.correo, password: 'clave12345' }).subscribe();

    const req = httpMock.expectOne(`${api}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ correo: usuario.correo, password: 'clave12345' });
    req.flush(respuestaLogin(jwt));

    expect(service.token()).toBe(jwt);
    expect(service.usuario()).toEqual(usuario);
    expect(service.isLoggedIn()).toBe(true);
    expect(service.esAnfitrion()).toBe(true);
    expect(service.verificado()).toBe(true);
    expect(localStorage.getItem('wm_token')).toBe(jwt);
  });

  it('no considera activa una sesión con el JWT vencido', () => {
    service.login({ correo: usuario.correo, password: 'clave12345' }).subscribe();

    httpMock.expectOne(`${api}/auth/login`).flush(respuestaLogin(token(true)));

    expect(service.token()).not.toBeNull();
    expect(service.isLoggedIn()).toBe(false);
  });

  it('actualiza los datos del usuario en sesión', () => {
    service.actualizarUsuario(usuario);

    expect(service.usuario()).toEqual(usuario);
    expect(service.verificado()).toBe(true);
    expect(localStorage.getItem('wm_usuario')).toContain('Ana Pérez');
  });

  it('cierra la sesión, borra el almacenamiento y redirige al login', () => {
    service.actualizarUsuario(usuario);

    service.logout();

    expect(service.token()).toBeNull();
    expect(service.usuario()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
    expect(localStorage.getItem('wm_token')).toBeNull();
    expect(localStorage.getItem('wm_usuario')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('puede cerrar la sesión sin redirigir (la usa el interceptor)', () => {
    service.actualizarUsuario(usuario);

    service.logout(false);

    expect(router.navigate).not.toHaveBeenCalled();
  });
});
