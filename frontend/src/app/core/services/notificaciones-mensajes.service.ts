import { DOCUMENT, DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { PRIMARY_OUTLET, Router } from '@angular/router';
import { EMPTY, Subject, catchError, exhaustMap, fromEvent, merge, of, switchMap, timer } from 'rxjs';
import { I18nService } from '../i18n/i18n.service';
import { MensajesNoLeidos } from '../models/conversacion.model';
import { AuthService } from './auth.service';
import { ConversacionService } from './conversacion.service';

const INTERVALO_AVISOS_MS = 5000;

/** US14 - Contador y avisos de la sesión autenticada. */
@Injectable()
export class NotificacionesMensajesService {
  private readonly documento = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly conversaciones = inject(ConversacionService);
  private readonly i18n = inject(I18nService);
  private readonly snack = inject(MatSnackBar);
  private readonly usuarioId = inject(AuthService).usuario()?.id;
  private readonly clavePreferencia = this.usuarioId === undefined ? null : `wm_no_molestar_${this.usuarioId}`;

  private readonly solicitudes = new Subject<void>();
  private readonly ultimosDetectados = new Map<string, number>();
  private readonly cantidad = signal(0);
  private readonly preferencia = signal(this.leerPreferencia());
  private aviso?: MatSnackBarRef<SimpleSnackBar>;

  readonly total = this.cantidad.asReadonly();
  readonly noMolestar = this.preferencia.asReadonly();

  constructor() {
    merge(
      of(undefined),
      this.solicitudes,
      fromEvent(window, 'online'),
      fromEvent(this.documento, 'visibilitychange'),
    ).pipe(
      switchMap(() => this.documento.visibilityState === 'visible'
        ? timer(0, INTERVALO_AVISOS_MS).pipe(
          exhaustMap(() => this.conversaciones.consultarNoLeidos().pipe(catchError(() => EMPTY))),
        )
        : EMPTY),
      takeUntilDestroyed(),
    ).subscribe((resumen) => this.recibir(resumen));

    this.destroyRef.onDestroy(() => this.aviso?.dismiss());
  }

  actualizar(): void {
    this.solicitudes.next();
  }

  cambiarNoMolestar(activado: boolean): void {
    this.preferencia.set(activado);
    if (activado) this.aviso?.dismiss();
    try {
      if (this.clavePreferencia) localStorage.setItem(this.clavePreferencia, String(activado));
    } catch {
      // La preferencia sigue activa durante la sesión.
    }
  }

  private recibir({ total, conversaciones }: MensajesNoLeidos): void {
    this.cantidad.set(total);
    const [seccion, id, grupoId] = this.router.parseUrl(this.router.url).root.children[PRIMARY_OUTLET]?.segments ?? [];
    const enMensajes = seccion?.path === 'mensajes';
    const enBandeja = enMensajes && !id;
    const chatAbierto = enMensajes && id
      ? (grupoId ? 'grupo:' + Number(grupoId.path) : 'directa:' + Number(id.path))
      : null;

    let hayNovedades = false;
    for (const { conversacionId, ultimoMensajeId, grupal } of conversaciones) {
      // Cada chat tiene su propio orden de llegada a la base de datos.
      const clave = (grupal ? 'grupo:' : 'directa:') + conversacionId;
      if (ultimoMensajeId <= (this.ultimosDetectados.get(clave) ?? 0)) continue;
      this.ultimosDetectados.set(clave, ultimoMensajeId);
      if (clave !== chatAbierto) hayNovedades = true;
    }

    if (hayNovedades && !enBandeja && !this.noMolestar()) {
      this.aviso = this.snack.open(this.i18n.t('mensajes.noLeidos'), this.i18n.t('mensajes.ver'), {
        duration: 7000,
        horizontalPosition: 'end',
        panelClass: 'wm-aviso-mensajes',
      });
      this.aviso.onAction().pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.router.navigate(['/mensajes']));
    }
  }

  private leerPreferencia(): boolean {
    try {
      return this.clavePreferencia !== null && localStorage.getItem(this.clavePreferencia) === 'true';
    } catch {
      return false;
    }
  }
}
