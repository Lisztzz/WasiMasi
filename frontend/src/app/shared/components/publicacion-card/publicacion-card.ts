import { Component, computed, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';
import { Publicacion } from '../../../core/models/publicacion.model';
import {
  avisosCondiciones, esCondicionNegociable, textoCoordenadas, textoGenero, textoMascotas, textoServicios,
} from '../../../core/utils/condiciones-publicacion';
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

  private readonly i18n = inject(I18nService);

  /** US09 - Condiciones negociables/no negociables. */
  protected readonly mascotasTexto = computed(() => textoMascotas(this.publicacion(), this.i18n));
  protected readonly generoTexto = computed(() => textoGenero(this.publicacion(), this.i18n));
  protected readonly serviciosTexto = computed(() => textoServicios(this.publicacion()));

  /** US09 - Las condiciones que no están en la lista de fijas se muestran como negociables. */
  protected readonly mascotasNegociable = computed(() => esCondicionNegociable(this.publicacion(), 'mascotas'));
  protected readonly generoNegociable = computed(() => esCondicionNegociable(this.publicacion(), 'genero'));
  protected readonly serviciosNegociable = computed(() => esCondicionNegociable(this.publicacion(), 'servicios'));
  protected readonly avisos = computed(() => avisosCondiciones(this.publicacion(), this.i18n));

  /** US10 - Coordenadas, suspendidas si la publicación no las tiene. */
  protected readonly coordenadasTexto = computed(() => textoCoordenadas(this.publicacion(), this.i18n));
}
