import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { I18nService } from '../../core/i18n/i18n.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { mensajeDeError } from '../../core/utils/api-error';
import { iniciales } from '../../shared/layout/main-layout/main-layout';

interface Paso {
  clave: string;
  hecho: boolean;
}

/** US03/US04/US05 - Perfil y verificación de identidad (mock-up 05). */
@Component({
  selector: 'app-perfil',
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule, TranslatePipe],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class Perfil implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly usuarios = inject(UsuarioService);
  private readonly snack = inject(MatSnackBar);
  private readonly i18n = inject(I18nService);

  protected readonly usuario = this.auth.usuario;
  protected readonly cargando = signal(true);
  protected readonly verificando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly iniciales = computed(() => iniciales(this.usuario()?.nombre));
  protected readonly verificado = computed(() => this.usuario()?.estadoVerificacion === 'VERIFICADO');

  protected readonly pasos = computed<Paso[]>(() => {
    const u = this.usuario();
    if (!u) return [];
    return [
      { clave: 'verif.paso.correo', hecho: true },
      { clave: u.rol === 'BUSCADOR' ? 'verif.paso.codigo' : 'verif.paso.dni', hecho: true },
      { clave: 'verif.paso.validacion', hecho: u.estadoVerificacion === 'VERIFICADO' },
    ];
  });

  ngOnInit(): void {
    // Siempre trae el estado real desde el backend (GET /usuarios/me)
    this.usuarios
      .obtenerPerfil()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({ error: (err) => this.error.set(mensajeDeError(err, this.i18n)) });
  }

  verificar(): void {
    const u = this.usuario();
    if (!u) return;
    this.verificando.set(true);
    this.error.set(null);
    this.usuarios
      .simularVerificacion(u.id)
      .pipe(finalize(() => this.verificando.set(false)))
      .subscribe({
        next: () => this.snack.open(this.i18n.t('verif.ok'), 'OK'),
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }

  cerrarSesion(): void {
    this.auth.logout();
  }
}
