import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { ConversacionResumen } from '../../../core/models/conversacion.model';
import { ConversacionService } from '../../../core/services/conversacion.service';
import { NotificacionesMensajesService } from '../../../core/services/notificaciones-mensajes.service';
import { mensajeDeError } from '../../../core/utils/api-error';

/** US12/US14 - Bandeja y preferencia de avisos de mensajes. */
@Component({
  selector: 'app-bandeja',
  imports: [DatePipe, MatIconModule, MatSlideToggleModule, RouterLink, TranslatePipe],
  templateUrl: './bandeja.html',
  styleUrl: '../mensajes.scss',
})
export class Bandeja {
  protected readonly notificaciones = inject(NotificacionesMensajesService);
  protected readonly filas = signal<ConversacionResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.notificaciones.actualizar();
    const i18n = inject(I18nService);
    inject(ConversacionService).listar()
      .pipe(takeUntilDestroyed(), finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (filas) => this.filas.set(filas),
        error: (err) => this.error.set(mensajeDeError(err, i18n)),
      });
  }
}
