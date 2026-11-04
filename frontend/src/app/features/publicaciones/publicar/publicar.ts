import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize, map, startWith } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Publicacion } from '../../../core/models/publicacion.model';
import { AuthService } from '../../../core/services/auth.service';
import { PublicacionService } from '../../../core/services/publicacion.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { filtrarDistritos } from '../../../core/utils/distritos';
import { PublicacionCard } from '../../../shared/components/publicacion-card/publicacion-card';

const MAX_FOTOS = 6;
const URL_IMAGEN = /^https?:\/\/\S+$/i;

/** US06 - Publicar una habitación (solo anfitriones, mock-up 06). */
@Component({
  selector: 'app-publicar',
  imports: [
    ReactiveFormsModule, RouterLink, MatAutocompleteModule, MatButtonModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule, TranslatePipe, PublicacionCard,
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

  protected readonly maxFotos = MAX_FOTOS;
  protected readonly verificado = this.auth.verificado;
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly fotos = signal<string[]>([]);
  protected readonly fotosFallidas = signal<ReadonlySet<string>>(new Set());
  protected readonly intentoEnviar = signal(false);

  protected readonly form = this.fb.group({
    zona: ['', Validators.required],
    precio: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    descripcion: ['', [Validators.required, Validators.maxLength(1000)]],
  });
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

  /** Vista previa en vivo de cómo se verá la tarjeta en la búsqueda. */
  protected readonly vistaPrevia = computed<Publicacion>(() => {
    const v = this.valores();
    const u = this.auth.usuario();
    return {
      id: 0,
      anfitrionId: u?.id ?? 0,
      anfitrionNombre: u?.nombre ?? '',
      anfitrionVerificado: this.verificado(),
      zona: v.zona?.trim() || this.i18n.t('publicar.previaZona'),
      precio: Number(v.precio) || 0,
      descripcion: v.descripcion?.trim() || this.i18n.t('publicar.previaDescripcion'),
      fotos: this.fotos(),
      estado: this.verificado() ? 'ACTIVA' : 'BORRADOR',
      fechaPublicacion: new Date().toISOString(),
      advertencia: null,
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

  quitarFoto(url: string): void {
    this.fotos.update((lista) => lista.filter((f) => f !== url));
  }

  marcarFotoFallida(url: string): void {
    this.fotosFallidas.update((s) => new Set(s).add(url));
  }

  enviar(): void {
    this.intentoEnviar.set(true);
    if (this.form.invalid || this.fotos().length === 0) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();

    this.publicaciones
      .publicar({ zona: v.zona, precio: Number(v.precio), descripcion: v.descripcion.trim(), fotos: this.fotos() })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (creada) => {
          // Si el anfitrión no está verificado, el backend la guarda como BORRADOR con una advertencia
          this.snack.open(creada.advertencia ?? this.i18n.t('publicar.exito'), 'OK', { duration: 6000 });
          this.router.navigate(['/publicaciones', creada.id]);
        },
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }
}
