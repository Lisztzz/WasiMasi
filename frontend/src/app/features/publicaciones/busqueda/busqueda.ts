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
import { MatSelectModule } from '@angular/material/select';
import { finalize, map, startWith } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { FiltrosBusqueda, Publicacion } from '../../../core/models/publicacion.model';
import { PublicacionService } from '../../../core/services/publicacion.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { ZONAS_POPULARES, filtrarDistritos } from '../../../core/utils/distritos';
import { MapaResultados } from '../../../shared/components/mapa-resultados/mapa-resultados';
import { PublicacionCard } from '../../../shared/components/publicacion-card/publicacion-card';

const PRECIO_MIN = 0;
const PRECIO_MAX = 2000;

/** El mínimo no puede superar al máximo (el backend responde 400 en ese caso). */
function rangoValido(grupo: AbstractControl): ValidationErrors | null {
  const min = grupo.get('precioMin')?.value;
  const max = grupo.get('precioMax')?.value;
  return min !== null && max !== null && Number(min) > Number(max) ? { rango: true } : null;
}

/** US08 + US09 + US10 - Buscar habitaciones y verlas en lista o en el mapa. */
@Component({
  selector: 'app-busqueda',
  imports: [
    ReactiveFormsModule, MatAutocompleteModule, MatButtonModule, MatChipsModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSelectModule, TranslatePipe,
    MapaResultados, PublicacionCard,
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
  protected readonly vista = signal<'lista' | 'mapa'>('lista');

  protected readonly form = this.fb.group(
    {
      zona: ['', Validators.required],
      precioMin: [PRECIO_MIN, [Validators.required, Validators.min(0)]],
      precioMax: [PRECIO_MAX, [Validators.required, Validators.min(1)]],
      // '' = sin filtrar; '1' = solo con mascotas; '0' = sin mascotas
      mascotas: [''],
      genero: [''],
      servicios: [''],
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
    // Los filtros viven en la URL (?zona=...&precioMin=...&mascotas=1...):
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

      const mascotasParam = params.get('mascotas');
      const generoParam = params.get('genero');
      const filtros: FiltrosBusqueda = {
        zona,
        precioMin: numero(params.get('precioMin'), PRECIO_MIN),
        precioMax: numero(params.get('precioMax'), PRECIO_MAX),
        mascotas: mascotasParam === '1' ? true : mascotasParam === '0' ? false : undefined,
        genero: generoParam === 'M' || generoParam === 'F' ? generoParam : undefined,
        servicios: params.get('servicios') || undefined,
      };
      this.form.setValue({
        zona,
        precioMin: filtros.precioMin,
        precioMax: filtros.precioMax,
        mascotas: mascotasParam === '1' ? '1' : mascotasParam === '0' ? '0' : '',
        genero: generoParam ?? '',
        servicios: filtros.servicios ?? '',
      }, { emitEvent: false });
      // Restaurar una URL o volver atrás solo recupera resultados; no cuenta como otra búsqueda.
      if (!mismaBusqueda(filtros, this.buscada())) {
        this.ejecutar(filtros, false);
      }
    });
  }

  buscar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const filtros: FiltrosBusqueda = {
      zona: v.zona.trim(),
      precioMin: Number(v.precioMin),
      precioMax: Number(v.precioMax),
      mascotas: v.mascotas === '' ? undefined : v.mascotas === '1',
      genero: v.genero === '' ? undefined : (v.genero as 'M' | 'F' | 'Any'),
      servicios: v.servicios.trim() || undefined,
    };
    this.ejecutar(filtros, true);
    this.irA(filtros);
  }

  buscarZona(zona: string): void {
    this.form.controls.zona.setValue(zona);
    this.buscar();
  }

  private irA(filtros: FiltrosBusqueda): void {
    // En la URL, mascotas va como '1'/'0' (así lo lee el constructor); los filtros vacíos no se escriben.
    const mascotas = filtros.mascotas === undefined ? undefined : filtros.mascotas ? '1' : '0';
    this.router.navigate([], { relativeTo: this.route, queryParams: { ...filtros, mascotas } });
  }

  private ejecutar(filtros: FiltrosBusqueda, registrar: boolean): void {
    this.cargando.set(true);
    this.error.set(null);
    this.buscada.set(filtros);
    (registrar ? this.publicaciones.buscar(filtros) : this.publicaciones.consultar(filtros))
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

function mismaBusqueda(a: FiltrosBusqueda, b: FiltrosBusqueda | null): boolean {
  return b !== null
    && a.zona === b.zona
    && a.precioMin === b.precioMin
    && a.precioMax === b.precioMax
    && a.mascotas === b.mascotas
    && (a.genero ?? '') === (b.genero ?? '')
    && (a.servicios ?? '') === (b.servicios ?? '');
}
