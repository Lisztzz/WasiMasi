import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Publicacion } from '../../../core/models/publicacion.model';
import { PublicacionService } from '../../../core/services/publicacion.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { SelloVerificacion } from '../../../shared/components/sello-verificacion/sello-verificacion';
import { iniciales } from '../../../shared/layout/main-layout/main-layout';

/** US11 - Detalle de una habitación (mock-up 04). */
@Component({
  selector: 'app-detalle',
  imports: [DecimalPipe, RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule, TranslatePipe, SelloVerificacion],
  templateUrl: './detalle.html',
  styleUrl: './detalle.scss',
})
export class Detalle {
  private readonly publicaciones = inject(PublicacionService);
  private readonly i18n = inject(I18nService);
  private readonly snack = inject(MatSnackBar);

  /** Parámetro :id de la ruta /publicaciones/:id */
  readonly id = input.required<string>();

  protected readonly publicacion = signal<Publicacion | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly fotoActual = signal(0);
  protected readonly fotosFallidas = signal<ReadonlySet<number>>(new Set());

  /** Para "Volver a resultados" con los mismos filtros. */
  protected readonly volverParams = computed(() => this.publicaciones.ultimaBusqueda() ?? {});
  protected readonly inicialesAnfitrion = computed(() => iniciales(this.publicacion()?.anfitrionNombre));
  protected readonly fecha = computed(() => {
    const p = this.publicacion();
    if (!p?.fechaPublicacion) return '';
    return new Date(p.fechaPublicacion).toLocaleDateString(this.i18n.lang() === 'es' ? 'es-PE' : 'en-US', {
      day: 'numeric', month: 'long', year: 'numeric',
    });
  });

  constructor() {
    effect(() => this.cargar(Number(this.id())));
  }

  private cargar(id: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.fotoActual.set(0);
    this.publicaciones
      .obtener(id)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (p) => this.publicacion.set(p),
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }

  marcarFotoFallida(indice: number): void {
    this.fotosFallidas.update((s) => new Set(s).add(indice));
  }

  /** Mensajería y visitas son del Sprint 3: por ahora solo se avisa. */
  proximamente(): void {
    this.snack.open(this.i18n.t('detalle.proximamente'), 'OK');
  }
}
