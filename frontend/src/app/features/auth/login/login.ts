import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import { AuthLayout } from '../../../shared/layout/auth-layout/auth-layout';

/** US02 - Inicio de sesión (mock-up 01 Login). */
@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatIconModule,
    MatInputModule, MatProgressSpinnerModule, TranslatePipe, AuthLayout,
  ],
  templateUrl: './login.html',
  styleUrl: '../auth-form.scss',
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  private readonly i18n = inject(I18nService);

  /** Parámetros de la URL: ?returnUrl=/perfil y ?expirada=1 */
  readonly returnUrl = input<string>();
  readonly expirada = input<string>();

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verPassword = signal(false);

  protected readonly form = this.fb.group({
    correo: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set(null);
    const { correo, password } = this.form.getRawValue();

    this.auth
      .login({ correo: correo.trim(), password })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (res) => {
          if (res.requiereVerificacion) {
            this.snack.open(this.i18n.t('login.pendiente'), 'OK', { duration: 6000 });
          }
          this.router.navigateByUrl(this.destinoSeguro());
        },
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }

  /** Solo rutas internas, para no redirigir a otro sitio. */
  private destinoSeguro(): string {
    const url = this.returnUrl();
    return url && url.startsWith('/') && !url.startsWith('//') ? url : '/';
  }
}
