import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models/usuario.model';
import { AuthService } from './auth.service';
import { UsuarioService } from './usuario.service';

/** US03/US04/US05 - Perfil y verificación simulada. */
describe('UsuarioService', () => {
  let service: UsuarioService;
  let httpMock: HttpTestingController;
  let auth: { actualizarUsuario: ReturnType<typeof vi.fn> };
  const api = environment.apiUrl;
  const usuario: Usuario = {
    id: 5,
    nombre: 'Luis',
    correo: 'luis@upc.edu.pe',
    rol: 'BUSCADOR',
    universidad: 'UPC',
    estadoVerificacion: 'VERIFICADO',
  };

  beforeEach(() => {
    auth = { actualizarUsuario: vi.fn() };
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });
    service = TestBed.inject(UsuarioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('obtiene el perfil y actualiza los datos de la sesión (US05)', () => {
    service.obtenerPerfil().subscribe((perfil) => expect(perfil).toEqual(usuario));

    const req = httpMock.expectOne(`${api}/usuarios/me`);
    expect(req.request.method).toBe('GET');
    req.flush(usuario);

    expect(auth.actualizarUsuario).toHaveBeenCalledWith(usuario);
  });

  it('simula la verificación por id y actualiza la sesión (US03/US04)', () => {
    service.simularVerificacion(5).subscribe((verificado) => expect(verificado).toEqual(usuario));

    const req = httpMock.expectOne(`${api}/usuarios/5/simular-verificacion`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(usuario);

    expect(auth.actualizarUsuario).toHaveBeenCalledWith(usuario);
  });
});
