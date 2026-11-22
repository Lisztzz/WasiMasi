import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { I18nService } from '../../../core/i18n/i18n.service';
import { Publicacion } from '../../../core/models/publicacion.model';
import { PublicacionCard } from './publicacion-card';

/** US09/US10/US11 - Tarjeta de resultados con condiciones, coordenadas y enlace al detalle. */
describe('PublicacionCard', () => {
  let fixture: ComponentFixture<PublicacionCard>;
  let i18n: I18nService;

  function publicacion(cambios: Partial<Publicacion> = {}): Publicacion {
    return {
      id: 7,
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
      ...cambios,
    };
  }

  function crear(cambios: Partial<Publicacion> = {}, vistaPrevia = false): HTMLElement {
    fixture.componentRef.setInput('publicacion', publicacion(cambios));
    fixture.componentRef.setInput('vistaPrevia', vistaPrevia);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [PublicacionCard],
      providers: [provideRouter([])],
    });
    i18n = TestBed.inject(I18nService);
    i18n.setLang('es');
    fixture = TestBed.createComponent(PublicacionCard);
  });

  afterEach(() => localStorage.clear());

  it('muestra precio, zona, descripción y anfitrión', () => {
    const html = crear();

    expect(html.querySelector('.precio')?.textContent).toContain('1,200');
    expect(html.querySelector('.zona')?.textContent).toContain('Miraflores');
    expect(html.querySelector('.descripcion')?.textContent).toContain('Habitación luminosa');
    expect(html.querySelector('.anfitrion')?.textContent).toContain('Ana');
    expect(html.querySelector('.anfitrion')?.textContent).toContain('Verificado');
  });

  it('muestra las condiciones negociables de US09', () => {
    const html = crear({ mascotasPermitidas: true, generoPreferido: 'F', servicios: 'agua, wifi' });
    const condiciones = html.querySelector('.condiciones')?.textContent ?? '';

    expect(condiciones).toContain('Mascotas: Sí');
    expect(condiciones).toContain('Género preferido: Femenino');
    expect(condiciones).toContain('Servicios incluidos: agua, wifi');
  });

  it('etiqueta como negociables solo las condiciones que el anfitrión dejó fuera de la lista (US09)', () => {
    const html = crear({
      mascotasPermitidas: true,
      generoPreferido: 'F',
      servicios: 'agua',
      condicionesNoNegociables: ['mascotas'],
    });
    const condiciones = html.querySelectorAll('.condiciones span');

    expect(condiciones[0].textContent).toContain('Mascotas: Sí');
    expect(condiciones[0].querySelector('.negociable')).toBeNull();
    expect(condiciones[1].querySelector('.negociable')?.textContent).toContain('Negociable');
    expect(condiciones[2].querySelector('.negociable')?.textContent).toContain('Negociable');
    expect(html.querySelector('.aviso-condiciones')).toBeNull();
  });

  it('muestra el aviso de las condiciones negociables que no cumplieron la búsqueda (US09)', () => {
    const html = crear({
      mascotasPermitidas: true,
      condicionesNoNegociables: [],
      condicionesNegociablesIncumplidas: ['mascotas', 'servicios'],
    });

    expect(html.querySelector('.aviso-condiciones')?.textContent)
      .toContain('Mascotas: no coincide con tu filtro');
    expect(html.querySelector('.aviso-condiciones')?.textContent)
      .toContain('Servicios: faltan servicios que pediste');
  });

  it('no muestra avisos ni etiquetas negociables en publicaciones anteriores a US09', () => {
    const html = crear({ mascotasPermitidas: true, condicionesNoNegociables: null });

    expect(html.querySelector('.negociable')).toBeNull();
    expect(html.querySelector('.aviso-condiciones')).toBeNull();
  });

  it('muestra las coordenadas de US10 cuando la publicación las tiene', () => {
    const html = crear({ latitud: -12.0464, longitud: -77.0428 });

    expect(html.querySelector('.coordenadas')?.textContent).toContain('Coordenadas: -12.0464, -77.0428');
  });

  it('no muestra la sección de coordenadas si no hay datos', () => {
    const html = crear();

    expect(html.querySelector('.coordenadas')).toBeNull();
  });

  it('enlaza al detalle con una etiqueta accesible', () => {
    const html = crear();
    const enlace = html.querySelector('a');

    expect(enlace?.getAttribute('href')).toBe('/publicaciones/7');
    expect(enlace?.getAttribute('aria-label')).toBe('Ver detalle de la habitación en Miraflores');
  });

  it('no muestra el enlace en la vista previa de publicar', () => {
    const html = crear({}, true);

    expect(html.querySelector('a')).toBeNull();
  });

  it('usa textos traducidos y el alt de la foto con i18n', () => {
    i18n.setLang('en');
    const html = crear({
      fotos: ['https://ejemplo.com/foto.jpg'],
      mascotasPermitidas: false,
      latitud: -12.0464,
      longitud: -77.0428,
    });

    expect(html.querySelector('img')?.getAttribute('alt')).toBe('Photo 1 of the room in Miraflores');
    expect(html.querySelector('.condiciones')?.textContent).toContain('Pets: No');
    expect(html.querySelector('.coordenadas')?.textContent).toContain('Coordinates: -12.0464, -77.0428');
  });
});
