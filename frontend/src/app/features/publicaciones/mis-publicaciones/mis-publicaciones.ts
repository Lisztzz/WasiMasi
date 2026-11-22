import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { EstadoGestionPublicacion, Publicacion } from '../../../core/models/publicacion.model';
import { PublicacionService } from '../../../core/services/publicacion.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { PublicacionCard } from '../../../shared/components/publicacion-card/publicacion-card';

/** US07 y US18 - Gestionar las habitaciones del anfitrión. */
@Component({
  selector: 'app-mis-publicaciones',
  imports: [RouterLink, MatButtonModule, MatIconModule, TranslatePipe, PublicacionCard],
  templateUrl: './mis-publicaciones.html',
  styleUrl: './mis-publicaciones.scss',
})
export class MisPublicaciones {
  private readonly servicio = inject(PublicacionService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly i18n = inject(I18nService);
  private readonly snack = inject(MatSnackBar);

  protected readonly publicaciones = signal<Publicacion[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly pendientes = signal<Record<number, boolean>>({});
  protected readonly erroresEstado = signal<Record<number, string | null>>({});

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.error.set(null);
    this.cargando.set(true);
    this.servicio.listarMias()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (publicaciones) => this.publicaciones.set(publicaciones),
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }

  protected cambiarEstado(id: number, estado: EstadoGestionPublicacion): void {
    if (this.pendientes()[id]) return;
    this.pendientes.update((pendientes) => ({ ...pendientes, [id]: true }));
    this.erroresEstado.update((errores) => ({ ...errores, [id]: null }));
    this.servicio.cambiarEstado(id, estado)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.pendientes.update((pendientes) => ({ ...pendientes, [id]: false }))),
      )
      .subscribe({
        next: (actualizada) => {
          this.publicaciones.update((lista) => lista.map((p) => p.id === id ? actualizada : p));
          this.snack.open(this.i18n.t('gestion.exito.' + actualizada.estado), this.i18n.t('gestion.cerrar'), { duration: 5000 });
        },
        error: (err) => this.erroresEstado.update((errores) => ({
          ...errores, [id]: mensajeDeError(err, this.i18n),
        })),
      });
  }
}
