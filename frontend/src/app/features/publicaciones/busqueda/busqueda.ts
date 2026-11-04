import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { finalize, map, startWith } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { FiltrosBusqueda, Publicacion } from '../../../core/models/publicacion.model';
import { PublicacionService } from '../../../core/services/publicacion.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { ZONAS_POPULARES, filtrarDistritos } from '../../../core/utils/distritos';
import { PublicacionCard } from '../../../shared/components/publicacion-card/publicacion-card';

const PRECIO_MIN = 0;
const PRECIO_MAX = 2000;

/** El mínimo no puede superar al máximo (el backend responde 400 en ese caso). */
function rangoValido(grupo: AbstractControl): ValidationErrors | null {
  const min = grupo.get('precioMin')?.value;
  const max = grupo.get('precioMax')?.value;
  return min !== null && max !== null && Number(min) > Number(max) ? { rango: true } : null;
}

/** US08 - Buscar habitaciones por zona y presupuesto (mock-up 03). */
@Component({
  selector: 'app-busqueda',
  imports: [
    ReactiveFormsModule, MatAutocompleteModule, MatButtonModule, MatChipsModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule, TranslatePipe, PublicacionCard,
  ],
  templateUrl: './busqueda.html',
  styleUrl: './busqueda.scss',
})
export class Busqueda {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly publicaciones = inject(PublicacionService);
  private readonly i18n = inject(I18nService);

  protected readonly zonasPopulares = ZONAS_POPULARES;
  protected readonly resultados = signal<Publicacion[] | null>(null);
  protected readonly buscada = signal<FiltrosBusqueda | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group(
    {
      zona: ['', Validators.required],
      precioMin: [PRECIO_MIN, [Validators.required, Validators.min(0)]],
      precioMax: [PRECIO_MAX, [Validators.required, Validators.min(1)]],
    },
    { validators: rangoValido },
  );

  protected readonly sugerencias = toSignal(
    this.form.controls.zona.valueChanges.pipe(startWith(''), map((texto) => filtrarDistritos(texto))),
    { initialValue: [] as string[] },
  );

  protected readonly textoResultados = computed(() => {
    const n = this.resultados()?.length ?? 0;
    return n === 1 ? this.i18n.t('buscar.resultado1') : this.i18n.t('buscar.resultados', { n });
  });

  constructor() {
    // Los filtros viven en la URL (?zona=...&precioMin=...&precioMax=...):
    // así el botón "atrás" del navegador y "Volver a resultados" conservan la búsqueda.
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const zona = params.get('zona');
      if (!zona) {
        const ultima = this.publicaciones.ultimaBusqueda();
        if (ultima) {
          this.irA(ultima);
        }
        return;
      }
      const filtros: FiltrosBusqueda = {
        zona,
        precioMin: numero(params.get('precioMin'), PRECIO_MIN),
        precioMax: numero(params.get('precioMax'), PRECIO_MAX),
      };
      this.form.setValue(filtros, { emitEvent: false });
      this.ejecutar(filtros);
    });
  }

  buscar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.irA({ zona: v.zona.trim(), precioMin: Number(v.precioMin), precioMax: Number(v.precioMax) });
  }

  buscarZona(zona: string): void {
    this.form.controls.zona.setValue(zona);
    this.buscar();
  }

  private irA(filtros: FiltrosBusqueda): void {
    this.router.navigate([], { relativeTo: this.route, queryParams: { ...filtros } });
  }

  private ejecutar(filtros: FiltrosBusqueda): void {
    this.cargando.set(true);
    this.error.set(null);
    this.buscada.set(filtros);
    this.publicaciones
      .buscar(filtros)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (lista) => this.resultados.set(lista),
        error: (err) => {
          this.resultados.set(null);
          this.error.set(mensajeDeError(err, this.i18n));
        },
      });
  }
}

function numero(valor: string | null, porDefecto: number): number {
  const n = Number(valor);
  return valor !== null && Number.isFinite(n) && n >= 0 ? n : porDefecto;
}
