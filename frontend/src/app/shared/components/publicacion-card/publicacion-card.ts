import { Component, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Publicacion } from '../../../core/models/publicacion.model';
import { SelloVerificacion } from '../sello-verificacion/sello-verificacion';

/** Tarjeta de una habitación en los resultados de búsqueda (mock-up 03). */
@Component({
  selector: 'app-publicacion-card',
  imports: [DecimalPipe, RouterLink, MatButtonModule, MatIconModule, TranslatePipe, SelloVerificacion],
  templateUrl: './publicacion-card.html',
  styleUrl: './publicacion-card.scss',
})
export class PublicacionCard {
  readonly publicacion = input.required<Publicacion>();
  /** En la pantalla de publicar la tarjeta es solo una vista previa: sin enlace al detalle. */
  readonly vistaPrevia = input(false);
  /** URL de la portada que no cargó (se muestra el ícono en su lugar). */
  protected readonly fotoFallida = signal<string | null>(null);
}
