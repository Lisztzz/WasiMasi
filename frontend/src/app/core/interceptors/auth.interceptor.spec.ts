import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';

/** Bearer token en las peticiones a la API y cierre de sesión en 401. */
describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let auth: { token: ReturnType<typeof vi.fn>; logout: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  const api = environment.apiUrl;

  beforeEach(() => {
    auth = { token: vi.fn(() => 'jwt-de-prueba'), logout: vi.fn() };
    router = { navigate: vi.fn() };
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('añade el Bearer a las peticiones de la API', () => {
    http.get(`${api}/publicaciones`).subscribe();

    const req = httpMock.expectOne(`${api}/publicaciones`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-de-prueba');
    req.flush([]);
  });

  it('no añade el token a /auth ni a URLs externas', () => {
    http.post(`${api}/auth/login`, {}).subscribe();
    http.get('https://ejemplo.com/otro-servicio').subscribe();

    const login = httpMock.expectOne(`${api}/auth/login`);
    const externa = httpMock.expectOne('https://ejemplo.com/otro-servicio');
    expect(login.request.headers.has('Authorization')).toBe(false);
    expect(externa.request.headers.has('Authorization')).toBe(false);
    login.flush({});
    externa.flush({});
  });

  it('no añade la cabecera cuando no hay token', () => {
    auth.token.mockReturnValue(null);

    http.get(`${api}/usuarios/me`).subscribe();

    const req = httpMock.expectOne(`${api}/usuarios/me`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('cierra la sesión y lleva al login con expirada=1 en un 401 de la API', async () => {
    const peticion = firstValueFrom(http.get(`${api}/publicaciones`));

    httpMock.expectOne(`${api}/publicaciones`).flush(
      { message: 'Token vencido' },
      { status: 401, statusText: 'Unauthorized' },
    );

    await expect(peticion).rejects.toMatchObject({ status: 401 });
    expect(auth.logout).toHaveBeenCalledWith(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { expirada: 1 } });
  });

  it('no cierra la sesión si el 401 viene de /auth', async () => {
    const peticion = firstValueFrom(http.post(`${api}/auth/login`, {}));

    httpMock.expectOne(`${api}/auth/login`).flush(
      { message: 'Credenciales inválidas' },
      { status: 401, statusText: 'Unauthorized' },
    );

    await expect(peticion).rejects.toMatchObject({ status: 401 });
    expect(auth.logout).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
