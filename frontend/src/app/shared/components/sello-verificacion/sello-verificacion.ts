import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

/** Sello verde "Verificado" o durazno "Sin verificar" del anfitrión. */
@Component({
  selector: 'app-sello-verificacion',
  imports: [MatIconModule, TranslatePipe],
  template: `
    <span class="sello" [class.ok]="verificado()">
      <mat-icon aria-hidden="true">{{ verificado() ? 'verified' : 'hourglass_top' }}</mat-icon>
      {{ (verificado() ? textoOk() : 'pub.pendiente') | t }}
    </span>
  `,
  styles: `
    .sello {
      display: inline-flex; align-items: center; gap: 4px; padding: 2px 10px; border-radius: 100px;
      background: var(--wm-container); color: var(--wm-on-container); font: var(--mat-sys-label-medium);
    }
    .sello.ok { background: var(--wm-success-container); color: var(--wm-success); }
    mat-icon { font-size: 16px; width: 16px; height: 16px; }
  `,
})
export class SelloVerificacion {
  readonly verificado = input.required<boolean>();
  /** Clave del texto cuando está verificado ("Verificado" o "Anfitrión verificado"). */
  readonly textoOk = input('pub.verificado');
}
