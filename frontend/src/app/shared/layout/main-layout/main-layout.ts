import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacionesMensajesService } from '../../../core/services/notificaciones-mensajes.service';
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
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive, NgTemplateOutlet,
    MatButtonModule, MatIconModule, TranslatePipe, Brand, LangSwitch,
  ],
  providers: [NotificacionesMensajesService],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
})
export class MainLayout {
  private readonly i18n = inject(I18nService);
  protected readonly auth = inject(AuthService);
  protected readonly notificaciones = inject(NotificacionesMensajesService);
  protected readonly landing = environment.landingUrl;

  /** Accesos de la barra según el rol de la cuenta. */
  protected readonly enlaces = computed<EnlaceNav[]>(() => [
    { ruta: '/buscar', icono: 'search', clave: 'nav.buscar', claveCorta: 'nav.buscarCorto' },
    ...(this.auth.esAnfitrion()
      ? [
          { ruta: '/publicar', icono: 'add', clave: 'nav.publicar', claveCorta: 'nav.publicarCorto' },
          { ruta: '/mis-publicaciones', icono: 'home_work', clave: 'gestion.titulo', claveCorta: 'gestion.navCorta' },
          { ruta: '/visitas', icono: 'event_available', clave: 'nav.visitas', claveCorta: 'nav.visitasCorto' },
          { ruta: '/reportes', icono: 'analytics', clave: 'nav.reportes', claveCorta: 'nav.reportesCorto' },
        ]
      : [{ ruta: '/asistente', icono: 'smart_toy', clave: 'nav.asistente', claveCorta: 'nav.asistenteCorto' }]),
    { ruta: '/mensajes', icono: 'chat', clave: 'mensajes.titulo', claveCorta: 'mensajes.titulo' },
    { ruta: '/perfil', icono: 'person', clave: 'nav.perfil', claveCorta: 'nav.perfilCorto' },
  ]);

  protected readonly iniciales = computed(() => iniciales(this.auth.usuario()?.nombre));
  protected readonly etiquetaMensajes = computed(() => {
    const total = this.notificaciones.total();
    return total > 0 ? this.i18n.t('mensajes.navNoLeidos', { total }) : null;
  });

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
