import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api-error.model';
import { I18nService } from '../i18n/i18n.service';

/** Convierte un error HTTP en un mensaje para mostrar al usuario. */
export function mensajeDeError(error: unknown, i18n: I18nService): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return i18n.t('error.red');
    }
    // El backend responde {timestamp, status, error, message}
    const cuerpo = error.error as Partial<ApiError> | null;
    if (cuerpo && typeof cuerpo.message === 'string' && cuerpo.message.trim()) {
      return cuerpo.message;
    }
  }
  return i18n.t('error.generico');
}
