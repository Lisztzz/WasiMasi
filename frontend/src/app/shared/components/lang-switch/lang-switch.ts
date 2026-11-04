import { Component, inject, input } from '@angular/core';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

/** Selector de idioma "ES | EN". */
@Component({
  selector: 'app-lang-switch',
  imports: [TranslatePipe],
  host: { '[class.claro]': 'claro()' },
  template: `
    <div class="lang" role="group" [attr.aria-label]="'lang.label' | t">
      <button type="button" (click)="i18n.setLang('es')" [attr.aria-pressed]="i18n.lang() === 'es'"
              [attr.aria-label]="'lang.cambiarEs' | t">ES</button>
      <span aria-hidden="true">|</span>
      <button type="button" (click)="i18n.setLang('en')" [attr.aria-pressed]="i18n.lang() === 'en'"
              [attr.aria-label]="'lang.cambiarEn' | t">EN</button>
    </div>
  `,
  styles: `
    .lang { display: inline-flex; align-items: center; gap: 2px; font: var(--mat-sys-label-large); color: var(--wm-primary); }
    :host(.claro) .lang { color: var(--wm-surface); }
    button { background: none; border: 0; padding: 4px 8px; border-radius: 8px; cursor: pointer; color: inherit; font: inherit; }
    button[aria-pressed='true'] { background: var(--wm-container); color: var(--wm-on-container); }
  `,
})
export class LangSwitch {
  protected readonly i18n = inject(I18nService);
  /** true cuando va sobre fondo azul marino */
  readonly claro = input(false);
}
