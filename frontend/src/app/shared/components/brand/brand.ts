import { Component, input } from '@angular/core';

/** Logo de WasiMasi (casa con dos roomies) + nombre. */
@Component({
  selector: 'app-brand',
  template: `
    <svg [attr.width]="size()" [attr.height]="size()" viewBox="0 0 64 64" aria-hidden="true" focusable="false">
      <path d="M8 28 L32 8 L56 28 V54 a4 4 0 0 1 -4 4 H12 a4 4 0 0 1 -4 -4 Z" fill="none"
            [attr.stroke]="claro() ? '#FFF7ED' : '#0F2D40'" stroke-width="5" stroke-linejoin="round" />
      <circle cx="25" cy="31" r="6" fill="#E8704F" />
      <circle cx="39" cy="31" r="6" fill="#E8704F" />
      <path d="M15 56 C15 44 19 39 25 39 C29 39 31 42 31 46 V56 Z" fill="#E8704F" />
      <path d="M49 56 C49 44 45 39 39 39 C35 39 33 42 33 46 V56 Z" fill="#E8704F" />
    </svg>
    <span class="nombre">WasiMasi</span>
  `,
  styles: `
    :host { display: inline-flex; align-items: center; gap: 10px; }
    .nombre { font: var(--mat-sys-title-large); color: var(--wm-primary); }
    :host(.claro) .nombre { color: var(--wm-surface); }
  `,
  host: { '[class.claro]': 'claro()' },
})
export class Brand {
  readonly size = input(40);
  readonly claro = input(false);
}
