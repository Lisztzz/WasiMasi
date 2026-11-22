import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { I18nService } from '../i18n/i18n.service';
import { mensajeDeError } from './api-error';

/** US02/US05 - Mensajes de error de la API visibles para el usuario. */
describe('mensajeDeError', () => {
  let i18n: I18nService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    i18n = TestBed.inject(I18nService);
    i18n.setLang('es');
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('usa el message del cuerpo cuando el backend responde con error JSON', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { timestamp: '2026-01-01', status: 409, error: 'Conflict', message: 'El correo ya está registrado' },
    });

    expect(mensajeDeError(error, i18n)).toBe('El correo ya está registrado');
  });

  it('muestra el mensaje de sin conexión cuando el status es 0', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(mensajeDeError(error, i18n)).toBe(i18n.t('error.red'));
  });

  it('usa el mensaje genérico si el cuerpo no trae message válido', () => {
    const sinMensaje = new HttpErrorResponse({ status: 500, error: { message: '   ' } });
    const sinCuerpo = new HttpErrorResponse({ status: 404, error: null });

    expect(mensajeDeError(sinMensaje, i18n)).toBe(i18n.t('error.generico'));
    expect(mensajeDeError(sinCuerpo, i18n)).toBe(i18n.t('error.generico'));
  });

  it('usa el mensaje genérico para errores que no son HTTP', () => {
    expect(mensajeDeError(new Error('boom'), i18n)).toBe(i18n.t('error.generico'));
    expect(mensajeDeError(null, i18n)).toBe(i18n.t('error.generico'));
  });

  it('traduce el mensaje genérico según el idioma activo', () => {
    i18n.setLang('en');

    expect(mensajeDeError(new Error('boom'), i18n)).toBe('Something went wrong. Please try again.');
  });
});
