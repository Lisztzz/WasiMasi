import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl, FormControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY, catchError, finalize, map, startWith, switchMap } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { ClaveCondicion, Publicacion } from '../../../core/models/publicacion.model';
import { AuthService } from '../../../core/services/auth.service';
import { PublicacionService } from '../../../core/services/publicacion.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { filtrarDistritos, normalizarZona } from '../../../core/utils/distritos';
import { PublicacionCard } from '../../../shared/components/publicacion-card/publicacion-card';

const MAX_FOTOS = 6;
const URL_IMAGEN = /^https?:\/\/\S+$/i;

/** Si se da una coordenada, la otra también: el mapa necesita el par completo. */
function coordenadasCompletas(grupo: AbstractControl): ValidationErrors | null {
  const lat = grupo.get('latitud')?.value;
  const lon = grupo.get('longitud')?.value;
  return (lat == null) !== (lon == null) ? { coordenadas: true } : null;
}

/** US06, US07, US09 y US10 - Publicar o editar una habitación propia. */
@Component({
  selector: 'app-publicar',
  imports: [
    ReactiveFormsModule, RouterLink, MatAutocompleteModule, MatButtonModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSelectModule, TranslatePipe, PublicacionCard,
  ],
  templateUrl: './publicar.html',
  styleUrl: './publicar.scss',
})
export class Publicar {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly publicaciones = inject(PublicacionService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  private readonly i18n = inject(I18nService);
  private readonly destroyRef = inject(DestroyRef);

  readonly id = input<string>();
  private readonly cambiosId = toObservable(this.id);
  protected readonly editando = computed(() => this.id() !== undefined);
  protected readonly errorCarga = signal<string | null>(null);
  protected readonly publicacionCargada = signal<Publicacion | null>(null);
  protected readonly maxFotos = MAX_FOTOS;
  protected readonly verificado = this.auth.verificado;
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly fotos = signal<string[]>([]);
  protected readonly fotosFallidas = signal<ReadonlySet<string>>(new Set());
  protected readonly intentoEnviar = signal(false);

  protected readonly form = this.fb.group({
    titulo: ['', Validators.maxLength(255)],
    zona: ['', Validators.required],
    precio: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    descripcion: ['', [Validators.required, Validators.maxLength(1000)]],
    // US09 - '' = no especificado, '1' = se permiten mascotas, '0' = no se permiten
    mascotas: [''],
    genero: [''],
    servicios: [''],
    // US09 - por defecto todas son rígidas; el anfitrión quita las que acepta negociar
    condicionesNoNegociables: this.fb.control<ClaveCondicion[]>(['mascotas', 'genero', 'servicios']),
    // US10 - opcionales, pero van juntas
    latitud: this.fb.control<number | null>(null, [Validators.min(-90), Validators.max(90)]),
    longitud: this.fb.control<number | null>(null, [Validators.min(-180), Validators.max(180)]),
  }, { validators: coordenadasCompletas });

  /** Campo para escribir la URL de una foto antes de agregarla a la lista. */
  protected readonly urlFoto = new FormControl('', { nonNullable: true, validators: Validators.pattern(URL_IMAGEN) });

  protected readonly sugerencias = toSignal(
    this.form.controls.zona.valueChanges.pipe(startWith(''), map((texto) => filtrarDistritos(texto))),
    { initialValue: [] as string[] },
  );
  private readonly valores = toSignal(this.form.valueChanges.pipe(startWith(this.form.getRawValue())), {
    initialValue: this.form.getRawValue(),
  });
  protected readonly largoDescripcion = computed(() => this.valores().descripcion?.length ?? 0);

  ngOnInit(): void {
    if (!this.editando()) return;
    this.form.controls.precio.setValidators([Validators.required, Validators.min(0.01)]);
    this.form.controls.descripcion.setValidators(Validators.maxLength(1000));

    this.cambiosId.pipe(
      switchMap((id) => {
        this.publicacionCargada.set(null);
        this.errorCarga.set(null);
        this.error.set(null);
        this.intentoEnviar.set(false);
        this.fotosFallidas.set(new Set());
        this.urlFoto.reset();
        if (id === undefined) return EMPTY;

        return this.publicaciones.listarMias().pipe(
          map((lista) => lista.find((p) => String(p.id) === id)),
          catchError((err) => {
            this.errorCarga.set(mensajeDeError(err, this.i18n));
            return EMPTY;
          }),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((publicacion) => {
      if (!publicacion) {
        this.errorCarga.set(this.i18n.t('gestion.noEncontrada'));
        return;
      }
      this.form.reset({
        titulo: publicacion.titulo ?? '',
        zona: publicacion.zona,
        precio: publicacion.precio,
        descripcion: publicacion.descripcion ?? '',
        mascotas: publicacion.mascotasPermitidas == null ? '' : publicacion.mascotasPermitidas ? '1' : '0',
        genero: publicacion.generoPreferido ?? '',
        servicios: publicacion.servicios ?? '',
        latitud: publicacion.latitud ?? null,
        longitud: publicacion.longitud ?? null,
        // null = publicación anterior a US09: todas sus condiciones se tratan como rígidas
        condicionesNoNegociables: publicacion.condicionesNoNegociables ?? ['mascotas', 'genero', 'servicios'],
      });
      this.fotos.set(publicacion.fotos);
      this.publicacionCargada.set(publicacion);
    });
  }

  /** Vista previa en vivo de cómo se verá la tarjeta en la búsqueda. */
  protected readonly vistaPrevia = computed<Publicacion>(() => {
    const v = this.valores();
    const u = this.auth.usuario();
    return {
      id: 0,
      anfitrionId: u?.id ?? 0,
      anfitrionNombre: u?.nombre ?? '',
      anfitrionVerificado: this.verificado(),
      titulo: this.editando() ? v.titulo?.trim() : undefined,
      zona: v.zona?.trim() || this.i18n.t('publicar.previaZona'),
      precio: Number(v.precio) || 0,
      descripcion: v.descripcion?.trim() || this.i18n.t('publicar.previaDescripcion'),
      fotos: this.fotos(),
      estado: this.verificado() ? 'ACTIVA' : 'BORRADOR',
      fechaPublicacion: new Date().toISOString(),
      advertencia: null,
      mascotasPermitidas: v.mascotas === '' ? undefined : v.mascotas === '1',
      generoPreferido: v.genero ? (v.genero as 'M' | 'F' | 'Any') : undefined,
      servicios: v.servicios?.trim() || undefined,
      condicionesNoNegociables: v.condicionesNoNegociables,
      latitud: v.latitud ?? undefined,
      longitud: v.longitud ?? undefined,
    };
  });

  agregarFoto(): void {
    const url = this.urlFoto.value.trim();
    if (!url || this.urlFoto.invalid || this.fotos().length >= MAX_FOTOS) {
      this.urlFoto.markAsTouched();
      return;
    }
    if (!this.fotos().includes(url)) {
      this.fotos.update((lista) => [...lista, url]);
    }
    this.urlFoto.reset();
  }

  quitarFoto(indice: number): void {
    this.fotos.update((lista) => lista.filter((_, i) => i !== indice));
  }

  marcarFotoFallida(url: string): void {
    this.fotosFallidas.update((s) => new Set(s).add(url));
  }

  enviar(): void {
    const publicacion = this.publicacionCargada();
    if (this.cargando() || (this.editando() && String(publicacion?.id) !== this.id())) return;

    this.intentoEnviar.set(true);
    if (this.form.invalid || this.fotos().length === 0) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();

    const datos = {
      zona: normalizarZona(v.zona),
      precio: Number(v.precio),
      descripcion: v.descripcion.trim(),
      fotos: this.fotos(),
      mascotasPermitidas: v.mascotas === '' ? undefined : v.mascotas === '1',
      generoPreferido: v.genero ? (v.genero as 'M' | 'F' | 'Any') : undefined,
      servicios: v.servicios.trim() || undefined,
      condicionesNoNegociables: v.condicionesNoNegociables,
      latitud: v.latitud ?? undefined,
      longitud: v.longitud ?? undefined,
    };
    const guardado = publicacion
      ? this.publicaciones.editar(publicacion.id, { ...datos, titulo: v.titulo.trim() || null })
      : this.publicaciones.publicar(datos);

    guardado
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (creada) => {
          if (publicacion) {
            this.snack.open(this.i18n.t('gestion.edicionGuardada'), this.i18n.t('gestion.cerrar'), { duration: 5000 });
            this.router.navigate(['/mis-publicaciones']);
            return;
          }
          // Si el anfitrión no está verificado, el backend la guarda como BORRADOR con una advertencia
          this.snack.open(creada.advertencia ?? this.i18n.t('publicar.exito'), 'OK', { duration: 6000 });
          this.router.navigate(['/publicaciones', creada.id]);
        },
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }
}