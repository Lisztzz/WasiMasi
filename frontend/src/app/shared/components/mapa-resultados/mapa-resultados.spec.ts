import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Publicacion } from '../../../core/models/publicacion.model';
import { MapaResultados } from './mapa-resultados';

/** US10 - El mapa debe degradarse sin problemas cuando no hay coordenadas. */
describe('MapaResultados', () => {
  let fixture: ComponentFixture<MapaResultados>;

  function publicacion(): Publicacion {
    return {
      id: 1,
      anfitrionId: 2,
      anfitrionNombre: 'Ana',
      anfitrionVerificado: true,
      zona: 'Miraflores',
      precio: 1200,
      descripcion: 'Habitación luminosa',
      fotos: [],
      estado: 'ACTIVA',
      fechaPublicacion: '2026-01-01',
      advertencia: null,
    };
  }

  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ imports: [MapaResultados] });
    fixture = TestBed.createComponent(MapaResultados);
  });

  afterEach(() => {
    fixture.destroy();
    localStorage.clear();
  });

  it('muestra el aviso de mapa vacío y su etiqueta accesible', () => {
    fixture.detectChanges();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.querySelector('section')?.getAttribute('aria-label')).toBe('Mapa de habitaciones');
    expect(html.querySelector('.sin-resultados')?.textContent).toContain('Ninguna publicación tiene coordenadas');
  });

  it('no intenta dibujar marcadores si las publicaciones no tienen coordenadas', () => {
    fixture.componentRef.setInput('publicaciones', [publicacion(), { ...publicacion(), id: 2 }]);
    fixture.detectChanges();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.querySelector('.sr-only')?.textContent).toContain('2 habitaciones');
    expect(html.querySelector('.sin-resultados')).toBeNull();
  });
});
