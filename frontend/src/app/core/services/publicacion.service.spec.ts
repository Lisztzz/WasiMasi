import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { FiltrosBusqueda, PublicacionRequest } from '../models/publicacion.model';
import { PublicacionService } from './publicacion.service';

/** US06/US08/US09/US10 - Parámetros y cuerpo enviados a la API de publicaciones. */
describe('PublicacionService', () => {
  let service: PublicacionService;
  let httpMock: HttpTestingController;
  const api = `${environment.apiUrl}/publicaciones`;

  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PublicacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('busca normalizando la zona y enviando sólo los parámetros obligatorios', () => {
    const filtros: FiltrosBusqueda = { zona: '  Jesús María ', precioMin: 500, precioMax: 1500 };
    let resultado: unknown;

    service.buscar(filtros).subscribe((publicaciones) => (resultado = publicaciones));

    const req = httpMock.expectOne((peticion) => peticion.url === api);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('zona')).toBe('Jesus Maria');
    expect(req.request.params.get('precioMin')).toBe('500');
    expect(req.request.params.get('precioMax')).toBe('1500');
    expect(req.request.params.keys().sort()).toEqual(['precioMax', 'precioMin', 'zona']);
    req.flush([]);

    expect(resultado).toEqual([]);
    expect(service.ultimaBusqueda()).toBe(filtros);
  });

  it('envía los filtros opcionales de US09, incluso mascotas en false', () => {
    const filtros: FiltrosBusqueda = {
      zona: 'Barranco',
      precioMin: 0,
      precioMax: 2000,
      mascotas: false,
      genero: 'F',
      servicios: 'agua,wifi',
    };

    service.buscar(filtros).subscribe();

    const req = httpMock.expectOne((peticion) => peticion.url === api);
    expect(req.request.params.get('mascotas')).toBe('false');
    expect(req.request.params.get('genero')).toBe('F');
    expect(req.request.params.get('servicios')).toBe('agua,wifi');
    expect(req.request.params.keys().sort()).toEqual([
      'genero', 'mascotas', 'precioMax', 'precioMin', 'servicios', 'zona',
    ]);
    req.flush([]);
  });

  it('omite filtros opcionales vacíos para no cambiar el contrato anterior', () => {
    service.buscar({ zona: 'Miraflores', precioMin: 100, precioMax: 900, servicios: '' })
      .subscribe();

    const req = httpMock.expectOne((peticion) => peticion.url === api);
    expect(req.request.params.keys().sort()).toEqual(['precioMax', 'precioMin', 'zona']);
    req.flush([]);
  });

  it('obtiene una publicación por id', () => {
    service.obtener(42).subscribe();

    const req = httpMock.expectOne(`${api}/42`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 42 });
  });

  it('publica normalizando la zona e incluyendo condiciones y coordenadas (US06/US09/US10)', () => {
    const datos: PublicacionRequest = {
      zona: '  San Isidro ',
      precio: 1800,
      descripcion: 'Cerca de la universidad',
      fotos: ['https://ejemplo.com/foto.jpg'],
      mascotasPermitidas: true,
      generoPreferido: 'Any',
      servicios: 'agua,electricidad,wifi',
      condicionesNoNegociables: ['mascotas', 'servicios'],
      latitud: -12.0971,
      longitud: -77.0353,
    };

    service.publicar(datos).subscribe();

    const req = httpMock.expectOne(api);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ ...datos, zona: 'San Isidro' });
    req.flush({ id: 7 });
  });

  it('deja pasar el error HTTP al llamador', () => {
    let errorRecibido: unknown;

    service.obtener(99).subscribe({ error: (error) => (errorRecibido = error) });

    httpMock.expectOne(`${api}/99`).flush(
      { status: 404, message: 'Publicación no encontrada' },
      { status: 404, statusText: 'Not Found' },
    );

    expect(errorRecibido).toBeInstanceOf(HttpErrorResponse);
    expect((errorRecibido as HttpErrorResponse).status).toBe(404);
  });
});
