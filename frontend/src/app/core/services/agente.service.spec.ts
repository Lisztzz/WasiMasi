import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AgenteService } from './agente.service';

describe('AgenteService', () => {
  const api = `${environment.apiUrl}/agente`;
  let service: AgenteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AgenteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('consulta al agente con el mensaje y el historial (US21)', () => {
    const historial = [{ rol: 'USUARIO' as const, contenido: 'Busco en Surco' }];
    let respuesta: unknown;
    service.consultar({ mensaje: 'hasta 700', historial }).subscribe((r) => (respuesta = r));

    const req = httpMock.expectOne(`${api}/consultas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ mensaje: 'hasta 700', historial });
    req.flush({ respuesta: 'Encontré 1', publicaciones: [], accionPendiente: null });

    expect(respuesta).toEqual({ respuesta: 'Encontré 1', publicaciones: [], accionPendiente: null });
  });

  it('confirma y cancela acciones por su id (US22)', () => {
    service.confirmar(30).subscribe();
    const confirmar = httpMock.expectOne(`${api}/acciones/30/confirmar`);
    expect(confirmar.request.method).toBe('POST');
    confirmar.flush({ id: 30, estado: 'CONFIRMADA' });

    service.cancelar(31).subscribe();
    const cancelar = httpMock.expectOne(`${api}/acciones/31/cancelar`);
    expect(cancelar.request.method).toBe('POST');
    cancelar.flush({ id: 31, estado: 'CANCELADA' });
  });
});
