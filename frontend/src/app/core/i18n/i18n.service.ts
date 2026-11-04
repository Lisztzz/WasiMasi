import { DOCUMENT, Injectable, effect, inject, signal } from '@angular/core';
import { Lang, TRANSLATIONS } from './translations';

const STORAGE_KEY = 'wm_lang';

/** Idioma de la interfaz (ES/EN). Recuerda la elección del usuario en el navegador. */
@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly document = inject(DOCUMENT);
  readonly lang = signal<Lang>(leerIdiomaGuardado());

  constructor() {
    effect(() => {
      const lang = this.lang();
      // Los lectores de pantalla usan el atributo lang para pronunciar bien
      this.document.documentElement.lang = lang;
      try {
        localStorage.setItem(STORAGE_KEY, lang);
      } catch {
        // Sin almacenamiento (modo privado): el idioma dura solo esta sesión
      }
    });
  }

  setLang(lang: Lang): void {
    this.lang.set(lang);
  }

  /** Devuelve el texto de la clave en el idioma actual. Ej: t('perfil.hola', { nombre: 'Ana' }) */
  t(key: string, params?: Record<string, string | number>): string {
    let texto = TRANSLATIONS[this.lang()][key] ?? TRANSLATIONS.es[key] ?? key;
    if (params) {
      for (const [nombre, valor] of Object.entries(params)) {
        texto = texto.replaceAll(`{${nombre}}`, String(valor));
      }
    }
    return texto;
  }
}

function leerIdiomaGuardado(): Lang {
  try {
    return localStorage.getItem(STORAGE_KEY) === 'en' ? 'en' : 'es';
  } catch {
    return 'es';
  }
}
