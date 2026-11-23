import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { EMPTY, Observable, Subject, catchError, exhaustMap, filter, fromEvent, merge, takeUntil, tap, timer } from 'rxjs';
import { I18nService } from '../i18n/i18n.service';
import { INTERVALO_CHAT_MS, Mensaje, MensajePendiente } from '../models/conversacion.model';
import { mensajeDeError } from '../utils/api-error';
import { AuthService } from './auth.service';
import { ConversacionService, esErrorTemporal } from './conversacion.service';

/** US13 - Cola de la cuenta activa, disponible al navegar dentro de la pestaña. */
@Injectable({ providedIn: 'root' })
export class EnvioMensajesService {
  private readonly auth = inject(AuthService);
  private readonly conversaciones = inject(ConversacionService);
  private readonly i18n = inject(I18nService);
  private readonly cuenta = toObservable(this.auth.usuario);
  private readonly cola = signal<MensajePendiente[]>([]);
  private readonly solicitudes = new Subject<void>();
  private readonly confirmados = new Subject<{ conversacionId: number; grupal: boolean; mensaje: Mensaje }>();

  readonly pendientes = this.cola.asReadonly();
  readonly enviados = this.confirmados.asObservable();

  constructor() {
    this.cuenta.pipe(takeUntilDestroyed()).subscribe((usuario) => {
      this.cola.update((cola) => cola.filter((envio) => envio.usuarioId === usuario?.id));
    });
    merge(this.solicitudes, fromEvent(window, 'online'), timer(INTERVALO_CHAT_MS, INTERVALO_CHAT_MS))
      .pipe(exhaustMap(() => this.procesar()), takeUntilDestroyed())
      .subscribe();
  }

  agregar(conversacionId: number, contenido: string, grupal: boolean): void {
    const usuarioId = this.auth.usuario()?.id;
    if (usuarioId === undefined) return;
    const envio: MensajePendiente = {
      clienteId: crypto.randomUUID(),
      conversacionId,
      grupal,
      usuarioId,
      contenido,
      estado: navigator.onLine ? 'enviando' : 'no-enviado',
      reintentar: true,
    };
    this.cola.update((cola) => [...cola, envio]);
    this.solicitudes.next();
  }

  confirmarDesdeHistorial(conversacionId: number, mensajes: Mensaje[], grupal: boolean): void {
    const usuarioId = this.auth.usuario()?.id;
    const ids = new Set(mensajes.filter((mensaje) => mensaje.emisorId === usuarioId).map((mensaje) => mensaje.clienteId));
    this.cola.update((cola) => cola.filter((envio) =>
      envio.conversacionId !== conversacionId || envio.grupal !== grupal ||
      envio.usuarioId !== usuarioId || !ids.has(envio.clienteId),
    ));
  }

  private procesar(): Observable<Mensaje> {
    const usuarioId = this.auth.usuario()?.id;
    if (!this.auth.isLoggedIn()) return EMPTY;
    const envio = this.cola().find((item) => item.usuarioId === usuarioId && item.reintentar);
    if (!envio) return EMPTY;

    if (!navigator.onLine) {
      if (envio.estado !== 'no-enviado') this.actualizar(envio, { estado: 'no-enviado' });
      return EMPTY;
    }

    return this.conversaciones.enviar(envio.conversacionId, envio.clienteId, envio.contenido, envio.grupal).pipe(
      takeUntil(this.cuenta.pipe(filter((usuario) => usuario?.id !== envio.usuarioId))),
      tap((mensaje) => {
        this.confirmados.next({ conversacionId: envio.conversacionId, grupal: envio.grupal, mensaje });
        this.confirmarDesdeHistorial(envio.conversacionId, [mensaje], envio.grupal);
        this.procesarColaDisponible();
      }),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === 401) return EMPTY;
        const reintentar = esErrorTemporal(error);
        this.actualizar(envio, {
          estado: 'no-enviado',
          reintentar,
          error: reintentar ? undefined : mensajeDeError(error, this.i18n),
        });
        if (!reintentar) this.procesarColaDisponible();
        return EMPTY;
      }),
    );
  }

  private actualizar(envio: MensajePendiente, cambios: Partial<MensajePendiente>): void {
    this.cola.update((cola) => cola.map((item) => item.clienteId === envio.clienteId ? { ...item, ...cambios } : item));
  }

  private procesarColaDisponible(): void {
    // Da tiempo a que exhaustMap cierre la petición antes de atender el siguiente mensaje.
    queueMicrotask(() => this.solicitudes.next());
  }
}
