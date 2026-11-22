import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, finalize } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Vivienda } from '../../../core/models/publicacion.model';
import { ViviendaService } from '../../../core/services/vivienda.service';
import { mensajeDeError } from '../../../core/utils/api-error';

/** US15 - Vincula una publicación existente a una vivienda del anfitrión. */
@Component({
  selector: 'app-vincular-vivienda',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, TranslatePipe],
  templateUrl: './vincular-vivienda.html',
  styles: [`
    :host, form { display: grid; gap: 12px; }
    h2, p { margin: 0; }
    p[role='alert'] { color: var(--wm-error); }
  `],
})
export class VincularVivienda {
  private readonly viviendas = inject(ViviendaService);
  private readonly i18n = inject(I18nService);
  private readonly snack = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly publicacionId = input.required<number>();
  readonly vinculada = output<number>();
  protected readonly opciones = signal<Vivienda[]>([]);
  protected readonly ocupada = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly nombre = new FormControl('', { nonNullable: true });
  protected readonly elegida = new FormControl<number | null>(null);

  constructor() {
    this.ejecutar(this.viviendas.listar(), (opciones) => this.opciones.set(opciones));
  }

  protected crear(evento: Event): void {
    evento.preventDefault();
    const nombre = this.nombre.value.trim();
    if (!nombre || this.ocupada()) return;
    this.ejecutar(this.viviendas.crear(nombre), (vivienda) => {
      this.opciones.update((opciones) => [...opciones, vivienda]);
      this.elegida.setValue(vivienda.id);
      this.nombre.reset();
    });
  }

  protected vincular(evento: Event): void {
    evento.preventDefault();
    const id = this.elegida.value;
    if (id === null || this.ocupada()) return;
    this.ejecutar(this.viviendas.vincular(this.publicacionId(), id), (vivienda) => {
      this.snack.open(this.i18n.t('vivienda.vinculada'), this.i18n.t('comun.cerrar'));
      this.vinculada.emit(vivienda.id);
    });
  }

  private ejecutar<T>(peticion: Observable<T>, procesar: (resultado: T) => void): void {
    this.ocupada.set(true);
    this.error.set(null);
    peticion.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupada.set(false))).subscribe({
      next: procesar,
      error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
    });
  }
}
