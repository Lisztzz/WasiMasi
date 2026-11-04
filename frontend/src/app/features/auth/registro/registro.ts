import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize, switchMap } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { RegistroRequest } from '../../../core/models/auth.model';
import { RolUsuario } from '../../../core/models/usuario.model';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { AuthLayout } from '../../../shared/layout/auth-layout/auth-layout';

/** US01 - Registro de buscador o anfitrión (mock-up 02 Registro). */
@Component({
  selector: 'app-registro',
  imports: [
    ReactiveFormsModule, RouterLink, MatButtonModule, MatChipsModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule, TranslatePipe, AuthLayout,
  ],
  templateUrl: './registro.html',
  styleUrl: '../auth-form.scss',
})
export class Registro {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  private readonly i18n = inject(I18nService);

  protected readonly roles: RolUsuario[] = ['BUSCADOR', 'ANFITRION'];
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verPassword = signal(false);

  protected readonly form = this.fb.group({
    rol: this.fb.control<RolUsuario>('BUSCADOR'),
    nombre: ['', Validators.required],
    correo: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    universidad: [''],
    codigoEstudiante: [''],
    documentoIdentidad: [''],
  });

  protected readonly rol = toSignal(this.form.controls.rol.valueChanges, { initialValue: 'BUSCADOR' as RolUsuario });

  constructor() {
    this.aplicarValidadores('BUSCADOR');
    let anterior: RolUsuario = 'BUSCADOR';
    this.form.controls.rol.valueChanges.pipe(takeUntilDestroyed()).subscribe((rol) => {
      // El chip seleccionado no se puede "deseleccionar": siempre debe haber un rol
      if (!rol) {
        this.form.controls.rol.setValue(anterior);
        return;
      }
      anterior = rol;
      this.aplicarValidadores(rol);
    });
  }

  /** Buscador: universidad y código. Anfitrión: DNI de 8 dígitos. */
  private aplicarValidadores(rol: RolUsuario): void {
    const { universidad, codigoEstudiante, documentoIdentidad } = this.form.controls;
    if (rol === 'BUSCADOR') {
      universidad.setValidators(Validators.required);
      codigoEstudiante.setValidators(Validators.required);
      documentoIdentidad.clearValidators();
    } else {
      universidad.clearValidators();
      codigoEstudiante.clearValidators();
      documentoIdentidad.setValidators([Validators.required, Validators.pattern(/^\d{8}$/)]);
    }
    for (const control of [universidad, codigoEstudiante, documentoIdentidad]) {
      control.updateValueAndValidity({ emitEvent: false });
    }
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);

    const v = this.form.getRawValue();
    const datos: RegistroRequest = {
      nombre: v.nombre.trim(),
      correo: v.correo.trim(),
      password: v.password,
      rol: v.rol,
      ...(v.rol === 'BUSCADOR'
        ? { universidad: v.universidad.trim(), codigoEstudiante: v.codigoEstudiante.trim() }
        : { documentoIdentidad: v.documentoIdentidad.trim() }),
    };

    // Después de registrarse inicia sesión automáticamente y va a su perfil para verificarse
    this.auth
      .registrar(datos)
      .pipe(
        switchMap(() => this.auth.login({ correo: datos.correo, password: datos.password })),
        finalize(() => this.cargando.set(false)),
      )
      .subscribe({
        next: () => {
          this.snack.open(this.i18n.t('registro.exito'), 'OK');
          this.router.navigate(['/perfil']);
        },
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }
}
