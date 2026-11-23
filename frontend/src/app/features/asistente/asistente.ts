import { DatePipe } from '@angular/common';
import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { I18nService } from '../../core/i18n/i18n.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { AccionAgente, MensajeAgente } from '../../core/models/agente.model';
import { Publicacion } from '../../core/models/publicacion.model';
import { AgenteService } from '../../core/services/agente.service';
import { AuthService } from '../../core/services/auth.service';
import { mensajeDeError } from '../../core/utils/api-error';
import { PublicacionCard } from '../../shared/components/publicacion-card/publicacion-card';

/** Un turno del chat con el asistente. */
interface Turno {
  id: number;
  rol: 'USUARIO' | 'ASISTENTE';
  texto: string;
  publicaciones?: Publicacion[];
  accion?: AccionAgente | null;
  error?: boolean;
}

const MAX_MENSAJE = 1000;

/** Trozo de una respuesta: el modelo marca con **texto** lo que quiere resaltar. */
export interface Parte {
  texto: string;
  negrita: boolean;
}

/**
 * Convierte el formato mínimo del modelo (**negrita**) en partes que Angular pinta
 * como texto. No se usa innerHTML: así nunca se interpreta HTML que venga del modelo.
 */
export function partesDeRespuesta(texto: string): Parte[] {
  return texto
    .split(/\*\*(.+?)\*\*/g)
    .map((t, i) => ({ texto: t, negrita: i % 2 === 1 }))
    .filter((p) => p.texto.length > 0);
}
const TURNOS_DE_CONTEXTO = 10;

/** US21 - Consultar al agente de IA en lenguaje natural. US22 - Confirmar la acción que propone. */
@Component({
  selector: 'app-asistente',
  imports: [
    DatePipe, ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatIconModule,
    MatInputModule, MatProgressSpinnerModule, TranslatePipe, PublicacionCard,
  ],
  templateUrl: './asistente.html',
  styleUrl: './asistente.scss',
})
export class Asistente {
  private readonly agente = inject(AgenteService);
  private readonly auth = inject(AuthService);
  private readonly i18n = inject(I18nService);
  private readonly snack = inject(MatSnackBar);
  private readonly lista = viewChild<ElementRef<HTMLElement>>('lista');

  protected readonly maximo = MAX_MENSAJE;
  protected readonly esBuscador = computed(() => this.auth.usuario()?.rol === 'BUSCADOR');
  protected readonly turnos = signal<Turno[]>([]);
  protected readonly pensando = signal(false);
  /** Acción que se está confirmando o cancelando (evita doble clic). */
  protected readonly resolviendo = signal<number | null>(null);
  protected readonly entrada = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(MAX_MENSAJE)],
  });

  protected readonly partes = partesDeRespuesta;
  protected readonly sugerencias = ['asistente.sugerencia1', 'asistente.sugerencia2', 'asistente.sugerencia3'];
  private siguienteId = 1;

  usarSugerencia(clave: string): void {
    this.entrada.setValue(this.i18n.t(clave));
    this.enviar();
  }

  enviar(evento?: Event): void {
    evento?.preventDefault();
    const texto = this.entrada.value.trim();
    if (!texto || this.pensando() || texto.length > MAX_MENSAJE) return;

    // El contexto son los turnos anteriores sin errores (texto plano, sin tarjetas).
    const historial: MensajeAgente[] = this.turnos()
      .filter((t) => !t.error && t.texto)
      .slice(-TURNOS_DE_CONTEXTO)
      .map((t) => ({ rol: t.rol, contenido: t.texto.slice(0, 2000) }));

    this.agregar({ rol: 'USUARIO', texto });
    this.entrada.reset();
    this.pensando.set(true);

    this.agente
      .consultar({ mensaje: texto, historial })
      .pipe(finalize(() => this.pensando.set(false)))
      .subscribe({
        next: (r) => this.agregar({
          rol: 'ASISTENTE',
          texto: r.respuesta || this.i18n.t('asistente.sinTexto'),
          publicaciones: r.publicaciones,
          accion: r.accionPendiente,
        }),
        error: (err) => this.agregar({ rol: 'ASISTENTE', texto: mensajeDeError(err, this.i18n), error: true }),
      });
  }

  /** US22 - El usuario confirma: recién aquí el backend crea la solicitud de visita. */
  confirmar(turno: Turno, accion: AccionAgente): void {
    this.resolver(turno, accion, true);
  }

  cancelar(turno: Turno, accion: AccionAgente): void {
    this.resolver(turno, accion, false);
  }

  private resolver(turno: Turno, accion: AccionAgente, confirmar: boolean): void {
    if (this.resolviendo() !== null) return;
    this.resolviendo.set(accion.id);
    const peticion = confirmar ? this.agente.confirmar(accion.id) : this.agente.cancelar(accion.id);
    peticion.pipe(finalize(() => this.resolviendo.set(null))).subscribe({
      next: (actualizada) => {
        this.turnos.update((lista) => lista.map((t) => (t.id === turno.id ? { ...t, accion: actualizada } : t)));
        this.snack.open(this.i18n.t(confirmar ? 'asistente.visitaEnviada' : 'asistente.propuestaCancelada'), 'OK');
      },
      error: (err) => this.snack.open(mensajeDeError(err, this.i18n), 'OK'),
    });
  }

  limpiar(): void {
    this.turnos.set([]);
  }

  private agregar(turno: Omit<Turno, 'id'>): void {
    this.turnos.update((lista) => [...lista, { ...turno, id: this.siguienteId++ }]);
    // Espera a que Angular pinte el turno y baja hasta el final.
    setTimeout(() => {
      const el = this.lista()?.nativeElement;
      el?.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
    });
  }
}
