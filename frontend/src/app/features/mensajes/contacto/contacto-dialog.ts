import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Publicacion } from '../../../core/models/publicacion.model';
import { ConversacionService } from '../../../core/services/conversacion.service';
import { mensajeDeError } from '../../../core/utils/api-error';

/** US12 - Formulario para enviar el primer mensaje. */
@Component({
  selector: 'app-contacto-dialog',
  imports: [
    ReactiveFormsModule, MatButtonModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, TranslatePipe,
  ],
  templateUrl: './contacto-dialog.html',
  styles: [`
    mat-form-field { width: 100%; }
    p[role='alert'] { color: var(--wm-error); margin: 0; }
  `],
})
export class ContactoDialog {
  private readonly conversaciones = inject(ConversacionService);
  private readonly i18n = inject(I18nService);
  private readonly dialogRef = inject<MatDialogRef<ContactoDialog, number>>(MatDialogRef);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly publicacion = inject<Publicacion>(MAT_DIALOG_DATA);

  protected readonly maximo = 1000;
  protected readonly mensaje = new FormControl('', {
    nonNullable: true,
    validators: [Validators.maxLength(this.maximo)],
  });
  private readonly texto = toSignal(this.mensaje.valueChanges, { initialValue: '' });
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly puedeEnviar = computed(() =>
    !this.enviando() && this.texto().trim().length > 0 && this.texto().length <= this.maximo,
  );

  enviar(evento: Event): void {
    evento.preventDefault();
    if (!this.puedeEnviar()) return;
    this.enviando.set(true);
    this.error.set(null);
    this.conversaciones.iniciarContacto(this.publicacion.id, this.mensaje.value.trim())
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.enviando.set(false)))
      .subscribe({
        next: ({ conversacionId }) => this.dialogRef.close(conversacionId),
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }
}
