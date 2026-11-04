import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../../environments/environment';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Brand } from '../../components/brand/brand';
import { LangSwitch } from '../../components/lang-switch/lang-switch';

/** Plantilla de login y registro: panel de marca a la izquierda y formulario a la derecha (mock-ups 01 y 02). */
@Component({
  selector: 'app-auth-layout',
  imports: [MatIconModule, TranslatePipe, Brand, LangSwitch],
  templateUrl: './auth-layout.html',
  styleUrl: './auth-layout.scss',
})
export class AuthLayout {
  protected readonly landing = environment.landingUrl;
  protected readonly beneficios = ['auth.marca.b1', 'auth.marca.b2', 'auth.marca.b3'];
}
