import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { Interesado, EstadoVisita, Visita } from '../../../core/models/visita.model';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { VisitaService } from '../../../core/services/visita.service';
import { mensajeDeError } from '../../../core/utils/api-error';

type FiltroVisita = 'TODAS' | EstadoVisita;

/** US17/US19 - Bandeja de solicitudes y comparación de interesados. */
@Component({
  selector: 'app-gestion-visitas',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, TranslatePipe],
  templateUrl: './gestion-visitas.html',
  styleUrl: './gestion-visitas.scss',
})
export class GestionVisitas {
  private readonly api = inject(VisitaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly snack = inject(MatSnackBar);
  private readonly i18n = inject(I18nService);

  protected readonly visitas = signal<Visita[]>([]);
  protected readonly interesados = signal<Interesado[]>([]);
  protected readonly filtro = signal<FiltroVisita>('TODAS');
  protected readonly filtroPublicacionId = signal<number | null>(null);
  protected readonly cargando = signal(true);
  protected readonly cargandoInteresados = signal(false);
  protected readonly enviandoId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly errorInteresados = signal<string | null>(null);
  protected readonly publicacionComparada = signal<number | null>(null);
  protected readonly rechazoAbierto = signal<number | null>(null);
  protected readonly rechazoForm = this.fb.group({
    motivo: ['', [Validators.required, Validators.maxLength(500)]],
  });

  protected readonly visitasPublicacion = computed(() => {
    const id = this.filtroPublicacionId();
    return id === null ? this.visitas() : this.visitas().filter((visita) => visita.publicacionId === id);
  });
  protected readonly filtradas = computed(() => {
    const filtro = this.filtro();
    const visitas = this.visitasPublicacion();
    return filtro === 'TODAS' ? visitas : visitas.filter((visita) => visita.estado === filtro);
  });
  protected readonly pendientes = computed(() => this.visitasPublicacion().filter((visita) => visita.estado === 'PENDIENTE').length);
  protected readonly confirmadas = computed(() => this.visitasPublicacion().filter((visita) => visita.estado === 'CONFIRMADA').length);
  protected readonly rechazadas = computed(() => this.visitasPublicacion().filter((visita) => visita.estado === 'RECHAZADA').length);

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const valor = params.get('publicacionId');
      const id = valor && /^\d+$/.test(valor) ? Number(valor) : null;
      this.filtroPublicacionId.set(id && id > 0 ? id : null);
      if (id && id > 0 && this.publicacionComparada() !== id) {
        this.comparar(id);
      } else if (!id) {
        this.publicacionComparada.set(null);
      }
    });
    this.cargar();
  }

  /** US17/US19 - Quita el filtro para volver a la bandeja completa del anfitrión. */
  mostrarTodas(): void {
    this.router.navigate(['/visitas'], { replaceUrl: true });
  }

  /** US17 - Carga únicamente las visitas de publicaciones del anfitrión autenticado. */
  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.api.misVisitas().pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: (visitas) => this.visitas.set(visitas),
      error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
    });
  }

  /** US17 - Confirma una solicitud o abre el campo de motivo para rechazarla. */
  responder(visita: Visita, decision: 'CONFIRMADA' | 'RECHAZADA'): void {
    if (decision === 'RECHAZADA' && this.rechazoAbierto() !== visita.id) {
      this.rechazoForm.reset({ motivo: '' });
      this.rechazoAbierto.set(visita.id);
      return;
    }

    if (decision === 'RECHAZADA') {
      this.rechazoForm.markAllAsTouched();
      if (this.rechazoForm.invalid) return;
    }

    this.enviandoId.set(visita.id);
    this.api.responder(visita.id, {
      estado: decision,
      ...(decision === 'RECHAZADA' ? { motivoRespuesta: this.rechazoForm.controls.motivo.value.trim() } : {}),
    }).pipe(finalize(() => this.enviandoId.set(null))).subscribe({
      next: (actualizada) => {
        this.visitas.update((lista) => lista.map((actual) => actual.id === actualizada.id ? actualizada : actual));
        this.rechazoAbierto.set(null);
        this.snack.open(this.i18n.t('visitas.respuestaExitosa'), this.i18n.t('comun.cerrar'), { duration: 4500 });
      },
      error: (err) => this.snack.open(mensajeDeError(err, this.i18n), this.i18n.t('comun.cerrar'), { duration: 6000 }),
    });
  }

  /** US19 - Consulta todos los solicitantes que pidieron visitar la publicación. */
  comparar(publicacionId: number): void {
    this.publicacionComparada.set(publicacionId);
    this.errorInteresados.set(null);
    this.interesados.set([]);
    this.cargandoInteresados.set(true);
    this.api.interesados(publicacionId).pipe(finalize(() => this.cargandoInteresados.set(false))).subscribe({
      next: (interesados) => this.interesados.set(interesados),
      error: (err) => this.errorInteresados.set(mensajeDeError(err, this.i18n)),
    });
  }

  formatearFecha(valor: string | null): string {
    if (!valor) return this.i18n.t('visitas.sinFecha');
    return new Date(valor).toLocaleString(this.i18n.lang() === 'es' ? 'es-PE' : 'en-US', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  }
}
