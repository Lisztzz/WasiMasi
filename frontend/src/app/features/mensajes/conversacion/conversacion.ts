import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, afterRenderEffect, computed, inject, input, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { EMPTY, catchError, exhaustMap, retry, switchMap, tap, throwError, timer } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { ConversacionResumen, INTERVALO_CHAT_MS, Mensaje } from '../../../core/models/conversacion.model';
import { AuthService } from '../../../core/services/auth.service';
import { ConversacionService, esErrorTemporal } from '../../../core/services/conversacion.service';
import { EnvioMensajesService } from '../../../core/services/envio-mensajes.service';
import { NotificacionesMensajesService } from '../../../core/services/notificaciones-mensajes.service';
import { mensajeDeError } from '../../../core/utils/api-error';

/** US12/US13/US15 - Conversación directa o grupal con polling incremental. */
@Component({
  selector: 'app-conversacion',
  imports: [
    DatePipe, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule,
    ReactiveFormsModule, RouterLink, TranslatePipe,
  ],
  templateUrl: './conversacion.html',
  styleUrl: '../mensajes.scss',
})
export class Conversacion {
  private readonly auth = inject(AuthService);
  private readonly conversaciones = inject(ConversacionService);
  private readonly envios = inject(EnvioMensajesService);
  private readonly notificaciones = inject(NotificacionesMensajesService);
  private readonly i18n = inject(I18nService);
  private readonly historial = viewChild<ElementRef<HTMLOListElement>>('historial');

  readonly id = input.required<string>();
  readonly grupal = input(false);
  protected readonly chat = signal<ConversacionResumen | null>(null);
  protected readonly mensajes = signal<Mensaje[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly sinConexion = signal(false);
  protected readonly usuarioId = computed(() => this.auth.usuario()?.id);
  protected readonly pendientes = computed(() => this.envios.pendientes().filter((envio) =>
    envio.conversacionId === this.chat()?.id && envio.grupal === Boolean(this.grupal()) &&
    envio.usuarioId === this.usuarioId(),
  ));

  protected readonly maximo = 1000;
  protected readonly entrada = new FormControl('', { nonNullable: true });
  private readonly texto = toSignal(this.entrada.valueChanges, { initialValue: '' });
  protected readonly puedeEnviar = computed(() =>
    Boolean(this.chat()) && !this.chat()?.cerrada && this.texto().trim().length > 0 && this.texto().length <= this.maximo,
  );

  // Solo avanza con las respuestas de lectura, que contienen todos los IDs intermedios.
  private cursor = 0;
  private lecturaConfirmada = 0;
  private seguirFinal = true;

  constructor() {
    toObservable(this.id).pipe(
      switchMap((id) => {
        this.reiniciarVista();
        const conversacionId = Number(id);
        return this.abrir(conversacionId).pipe(
          switchMap(() => timer(0, INTERVALO_CHAT_MS).pipe(
            exhaustMap(() => this.sincronizar(conversacionId)),
          )),
          catchError((err) => {
            this.cargando.set(false);
            this.error.set(this.grupal() && err instanceof HttpErrorResponse && err.status === 403
              ? this.i18n.t('mensajes.visitaRequerida')
              : mensajeDeError(err, this.i18n));
            return EMPTY;
          }),
        );
      }),
      takeUntilDestroyed(),
    ).subscribe();

    this.envios.enviados.pipe(takeUntilDestroyed()).subscribe(({ conversacionId, grupal, mensaje }) => {
      if (conversacionId === this.chat()?.id && grupal === Boolean(this.grupal())) this.incorporar([mensaje]);
    });

    afterRenderEffect(() => {
      this.mensajes();
      this.pendientes();
      const lista = this.historial()?.nativeElement;
      if (lista && this.seguirFinal) lista.scrollTop = lista.scrollHeight;
    });
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    const chat = this.chat();
    if (!chat || !this.puedeEnviar()) return;
    this.seguirFinal = true;
    this.envios.agregar(chat.id, this.entrada.value.trim(), Boolean(this.grupal()));
    this.entrada.reset();
  }

  protected actualizarScroll(evento: Event): void {
    const lista = evento.currentTarget as HTMLElement;
    this.seguirFinal = lista.scrollHeight - lista.scrollTop - lista.clientHeight < 48;
  }

  private abrir(id: number) {
    return this.conversaciones.obtener(id, Boolean(this.grupal())).pipe(
      retry({
        delay: (error: unknown) => {
          if (!esErrorTemporal(error)) return throwError(() => error);
          this.cargando.set(false);
          this.sinConexion.set(true);
          return timer(INTERVALO_CHAT_MS);
        },
      }),
      tap(({ conversacion, mensajes }) => {
        this.chat.set(conversacion);
        this.recibir(id, mensajes);
        this.cargando.set(false);
        this.sinConexion.set(false);
      }),
    );
  }

  private sincronizar(id: number) {
    return this.conversaciones.nuevosMensajes(id, this.cursor, Boolean(this.grupal())).pipe(
      tap(({ mensajes, cerrada }) => {
        this.recibir(id, mensajes);
        if (cerrada && !this.chat()?.cerrada) this.chat.update((chat) => chat && { ...chat, cerrada });
        this.sinConexion.set(false);
      }),
      switchMap(() => this.marcarLectura(id)),
      catchError((error: unknown) => {
        if (!esErrorTemporal(error)) return throwError(() => error);
        this.sinConexion.set(true);
        return EMPTY;
      }),
    );
  }

  private marcarLectura(id: number) {
    const hasta = this.cursor;
    if (hasta <= this.lecturaConfirmada || document.visibilityState !== 'visible') return EMPTY;
    return this.conversaciones.marcarLectura(id, hasta, Boolean(this.grupal())).pipe(
      tap(() => {
        this.lecturaConfirmada = hasta;
        this.notificaciones.actualizar();
      }),
      catchError(() => EMPTY),
    );
  }

  private recibir(id: number, mensajes: Mensaje[]): void {
    const ultimo = mensajes.at(-1);
    if (!ultimo) return;
    this.cursor = ultimo.id;
    this.incorporar(mensajes);
    this.envios.confirmarDesdeHistorial(id, mensajes, Boolean(this.grupal()));
  }

  private incorporar(mensajes: Mensaje[]): void {
    this.mensajes.update((anteriores) => {
      const porId = new Map(anteriores.map((mensaje) => [mensaje.id, mensaje]));
      for (const mensaje of mensajes) porId.set(mensaje.id, mensaje);
      return [...porId.values()].sort((a, b) => a.id - b.id);
    });
  }

  private reiniciarVista(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.chat.set(null);
    this.mensajes.set([]);
    this.sinConexion.set(false);
    this.entrada.reset();
    this.cursor = 0;
    this.lecturaConfirmada = 0;
    this.seguirFinal = true;
  }
}
