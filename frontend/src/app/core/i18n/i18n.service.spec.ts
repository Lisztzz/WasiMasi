import { TestBed } from '@angular/core/testing';
import { I18nService } from './i18n.service';
import { TRANSLATIONS } from './translations';

/** Idioma de la interfaz (ES/EN): cambio, interpolación y claves faltantes. */
describe('I18nService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.lang = 'es';
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.lang = 'es';
  });

  it('empieza en español cuando no hay idioma guardado', () => {
    expect(TestBed.inject(I18nService).lang()).toBe('es');
  });

  it('recupera el idioma guardado en localStorage', () => {
    localStorage.setItem('wm_lang', 'en');

    expect(TestBed.inject(I18nService).lang()).toBe('en');
    expect(TestBed.inject(I18nService).t('login.boton')).toBe('Log in');
  });

  it('cambia los textos y el atributo lang al cambiar de idioma', () => {
    const i18n = TestBed.inject(I18nService);
    expect(i18n.t('login.boton')).toBe('Iniciar sesión');

    i18n.setLang('en');
    TestBed.flushEffects();

    expect(i18n.lang()).toBe('en');
    expect(i18n.t('login.boton')).toBe('Log in');
    expect(document.documentElement.lang).toBe('en');
    expect(localStorage.getItem('wm_lang')).toBe('en');
  });

  it('interpola parámetros de texto y número en ambos idiomas', () => {
    const i18n = TestBed.inject(I18nService);

    expect(i18n.t('buscar.resultados', { n: 3 })).toBe('3 habitaciones encontradas');
    expect(i18n.t('pub.fotoAlt', { n: 1, zona: 'Miraflores' })).toBe('Foto 1 de la habitación en Miraflores');

    i18n.setLang('en');
    expect(i18n.t('buscar.resultados', { n: 3 })).toBe('3 rooms found');
  });

  it('devuelve la propia clave cuando no existe en ningún idioma', () => {
    expect(TestBed.inject(I18nService).t('modulo.claveInexistente')).toBe('modulo.claveInexistente');
  });

  it('cae al español si la clave no existe en el idioma activo', () => {
    TRANSLATIONS.es['prueba.soloEs'] = 'Solo en español';
    try {
      const i18n = TestBed.inject(I18nService);
      i18n.setLang('en');

      expect(i18n.t('prueba.soloEs')).toBe('Solo en español');
    } finally {
      delete TRANSLATIONS.es['prueba.soloEs'];
    }
  });
});
