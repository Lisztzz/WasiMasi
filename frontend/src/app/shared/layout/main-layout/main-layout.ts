import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../../environments/environment';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { AuthService } from '../../../core/services/auth.service';
import { Brand } from '../../components/brand/brand';
import { LangSwitch } from '../../components/lang-switch/lang-switch';

interface EnlaceNav {
  ruta: string;
  icono: string;
  clave: string;
  claveCorta: string;
}

/** Plantilla de la app con sesión: barra superior en web y navegación inferior en mobile. */
@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule, TranslatePipe, Brand, LangSwitch],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
})
export class MainLayout {
  protected readonly auth = inject(AuthService);
  protected readonly landing = environment.landingUrl;

  /** Publicación, gestión, visitas y reportes disponibles para anfitriones. */
  protected readonly enlaces = computed<EnlaceNav[]>(() => [
    { ruta: '/buscar', icono: 'search', clave: 'nav.buscar', claveCorta: 'nav.buscarCorto' },
    ...(this.auth.esAnfitrion()
      ? [
          { ruta: '/publicar', icono: 'add', clave: 'nav.publicar', claveCorta: 'nav.publicarCorto' },
          { ruta: '/mis-publicaciones', icono: 'home_work', clave: 'gestion.titulo', claveCorta: 'gestion.navCorta' },
          { ruta: '/visitas', icono: 'event_available', clave: 'nav.visitas', claveCorta: 'nav.visitasCorto' },
          { ruta: '/reportes', icono: 'analytics', clave: 'nav.reportes', claveCorta: 'nav.reportesCorto' },
        ]
      : []),
    { ruta: '/perfil', icono: 'person', clave: 'nav.perfil', claveCorta: 'nav.perfilCorto' },
  ]);

  protected readonly iniciales = computed(() => iniciales(this.auth.usuario()?.nombre));

  salir(): void {
    this.auth.logout();
  }
}

export function iniciales(nombre: string | undefined | null): string {
  return (
    (nombre ?? '')
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte[0].toUpperCase())
      .join('') || '?'
  );
}
