import { TestBed } from '@angular/core/testing';
import { I18nService } from './i18n.service';
import { TranslatePipe } from './translate.pipe';

/** Pipe 't': traduce claves y reacciona al cambio de idioma. */
describe('TranslatePipe', () => {
  let pipe: TranslatePipe;
  let i18n: I18nService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    pipe = TestBed.runInInjectionContext(() => new TranslatePipe());
    i18n = TestBed.inject(I18nService);
    i18n.setLang('es');
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('traduce la clave al idioma actual', () => {
    expect(pipe.transform('nav.buscar')).toBe('Buscar habitaciones');
  });

  it('actualiza el texto cuando cambia el idioma (pipe impuro)', () => {
    expect(pipe.transform('nav.buscar')).toBe('Buscar habitaciones');

    i18n.setLang('en');

    expect(pipe.transform('nav.buscar')).toBe('Find rooms');
  });

  it('interpola parámetros', () => {
    expect(pipe.transform('buscar.vacio', { zona: 'Barranco' }))
      .toContain('Barranco');
  });

  it('devuelve la clave si no existe', () => {
    expect(pipe.transform('no.existe')).toBe('no.existe');
  });
});
