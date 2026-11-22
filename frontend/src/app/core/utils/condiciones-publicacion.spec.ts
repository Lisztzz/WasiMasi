import { TestBed } from '@angular/core/testing';
import { I18nService } from '../i18n/i18n.service';
import { Publicacion } from '../models/publicacion.model';
import {
  avisosCondiciones, esCondicionNegociable, textoCoordenadas, textoGenero, textoMascotas, textoServicios,
} from './condiciones-publicacion';

/** US09/US10 - Condiciones y coordenadas traducidas de una publicación. */
describe('condiciones-publicacion', () => {
  let i18n: I18nService;

  function publicacion(cambios: Partial<Publicacion> = {}): Publicacion {
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
      ...cambios,
    };
  }

  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    i18n = TestBed.inject(I18nService);
    i18n.setLang('es');
  });

  describe('textoMascotas', () => {
    it('devuelve Sí o No según la condición (US09)', () => {
      expect(textoMascotas(publicacion({ mascotasPermitidas: true }), i18n)).toBe('Sí');
      expect(textoMascotas(publicacion({ mascotasPermitidas: false }), i18n)).toBe('No');
    });

    it('devuelve null si no se especificó o llegó null', () => {
      expect(textoMascotas(publicacion(), i18n)).toBeNull();
      expect(textoMascotas(publicacion({ mascotasPermitidas: undefined }), i18n)).toBeNull();
    });

    it('usa el idioma activo', () => {
      i18n.setLang('en');

      expect(textoMascotas(publicacion({ mascotasPermitidas: true }), i18n)).toBe('Yes');
    });
  });

  describe('textoGenero', () => {
    it('traduce M, F y Any (US09)', () => {
      expect(textoGenero(publicacion({ generoPreferido: 'M' }), i18n)).toBe('Masculino');
      expect(textoGenero(publicacion({ generoPreferido: 'F' }), i18n)).toBe('Femenino');
      expect(textoGenero(publicacion({ generoPreferido: 'Any' }), i18n)).toBe('Cualquier');
    });

    it('devuelve null cuando no hay preferencia', () => {
      expect(textoGenero(publicacion(), i18n)).toBeNull();
    });

    it('traduce la preferencia al inglés', () => {
      i18n.setLang('en');

      expect(textoGenero(publicacion({ generoPreferido: 'Any' }), i18n)).toBe('Any');
    });
  });

  describe('textoServicios', () => {
    it('devuelve el texto tal cual, sin espacios de relleno', () => {
      expect(textoServicios(publicacion({ servicios: 'agua, wifi' }))).toBe('agua, wifi');
    });

    it('devuelve null si está vacío o ausente', () => {
      expect(textoServicios(publicacion({ servicios: '   ' }))).toBeNull();
      expect(textoServicios(publicacion())).toBeNull();
    });
  });

  describe('condiciones negociables (US09)', () => {
    it('marca como negociable solo la condición que no está en la lista de fijas', () => {
      const p = publicacion({ condicionesNoNegociables: ['mascotas'] });

      expect(esCondicionNegociable(p, 'mascotas')).toBe(false);
      expect(esCondicionNegociable(p, 'genero')).toBe(true);
      expect(esCondicionNegociable(p, 'servicios')).toBe(true);
    });

    it('trata null como publicación anterior a US09: todas son rígidas', () => {
      const p = publicacion({ condicionesNoNegociables: null });

      expect(esCondicionNegociable(p, 'mascotas')).toBe(false);
      expect(esCondicionNegociable(p, 'genero')).toBe(false);
      expect(esCondicionNegociable(p, 'servicios')).toBe(false);
    });

    it('traduce los avisos de condiciones negociables que no coincidieron', () => {
      const p = publicacion({ condicionesNegociablesIncumplidas: ['mascotas', 'servicios'] });

      expect(avisosCondiciones(p, i18n)).toEqual([
        'Mascotas: no coincide con tu filtro',
        'Servicios: faltan servicios que pediste',
      ]);

      i18n.setLang('en');
      expect(avisosCondiciones(p, i18n)[0]).toBe('Pets: does not match your filter');
    });

    it('devuelve ningún aviso cuando no hay condiciones que avisar', () => {
      expect(avisosCondiciones(publicacion(), i18n)).toEqual([]);
    });
  });

  describe('textoCoordenadas', () => {
    it('formatea latitud y longitud con cuatro decimales e i18n (US10)', () => {
      expect(textoCoordenadas(publicacion({ latitud: -12.04645, longitud: -77.04281 }), i18n))
        .toBe('Coordenadas: -12.0465, -77.0428');
    });

    it('devuelve null si falta alguna coordenada', () => {
      expect(textoCoordenadas(publicacion({ latitud: -12.0464 }), i18n)).toBeNull();
      expect(textoCoordenadas(publicacion({ longitud: -77.0428 }), i18n)).toBeNull();
      expect(textoCoordenadas(publicacion(), i18n)).toBeNull();
    });

    it('acepta coordenadas como texto y respeta el idioma', () => {
      i18n.setLang('en');

      expect(textoCoordenadas(
        publicacion({ latitud: '-12.0464' as unknown as number, longitud: '-77.0428' as unknown as number }),
        i18n,
      )).toBe('Coordinates: -12.0464, -77.0428');
    });
  });
});
