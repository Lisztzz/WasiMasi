import { Routes } from '@angular/router';
import { anfitrionGuard, authGuard, invitadoGuard } from './core/guards/auth.guards';
import { MainLayout } from './shared/layout/main-layout/main-layout';

export const routes: Routes = [
  // Públicas (si ya hay sesión, invitadoGuard manda al inicio)
  {
    path: 'login',
    canActivate: [invitadoGuard],
    title: 'Iniciar sesión · WasiMasi',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'registro',
    canActivate: [invitadoGuard],
    title: 'Crear cuenta · WasiMasi',
    loadComponent: () => import('./features/auth/registro/registro').then((m) => m.Registro),
  },
  // Con sesión: comparten la barra de navegación
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'buscar',
        title: 'Buscar habitaciones · WasiMasi',
        loadComponent: () => import('./features/publicaciones/busqueda/busqueda').then((m) => m.Busqueda),
      },
      {
        path: 'publicaciones/:id',
        title: 'Detalle de la habitación · WasiMasi',
        loadComponent: () => import('./features/publicaciones/detalle/detalle').then((m) => m.Detalle),
      },
      {
        path: 'publicar',
        canActivate: [anfitrionGuard],
        title: 'Publicar habitación · WasiMasi',
        loadComponent: () => import('./features/publicaciones/publicar/publicar').then((m) => m.Publicar),
      },
      {
        path: 'mis-publicaciones',
        canActivate: [anfitrionGuard],
        title: 'Mis publicaciones · WasiMasi',
        loadComponent: () => import('./features/publicaciones/mis-publicaciones/mis-publicaciones').then((m) => m.MisPublicaciones),
      },
      {
        path: 'mis-publicaciones/:id/editar',
        canActivate: [anfitrionGuard],
        title: 'Editar publicación · WasiMasi',
        loadComponent: () => import('./features/publicaciones/publicar/publicar').then((m) => m.Publicar),
      },
      {
        path: 'visitas',
        canActivate: [anfitrionGuard],
        title: 'Visitas e interesados · WasiMasi',
        loadComponent: () => import('./features/visitas/gestion-visitas/gestion-visitas').then((m) => m.GestionVisitas),
      },
      {
        path: 'reportes',
        canActivate: [anfitrionGuard],
        title: 'Reportes · WasiMasi',
        loadComponent: () => import('./features/reportes/dashboard-reportes/dashboard-reportes').then((m) => m.DashboardReportes),
      },
      {
        path: 'perfil',
        title: 'Mi perfil · WasiMasi',
        loadComponent: () => import('./features/perfil/perfil').then((m) => m.Perfil),
      },
      { path: '', pathMatch: 'full', redirectTo: 'buscar' },
    ],
  },
  { path: '**', redirectTo: '' },
];
