import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { anfitrionGuard, authGuard, invitadoGuard } from './auth.guards';

/** Rutas protegidas: sesión activa, invitados y rol de anfitrión. */
describe('auth.guards', () => {
  let auth: { isLoggedIn: ReturnType<typeof vi.fn>; esAnfitrion: ReturnType<typeof vi.fn>; logout: ReturnType<typeof vi.fn> };
  let router: { createUrlTree: ReturnType<typeof vi.fn> };
  const destino = { url: '/publicaciones' } as RouterStateSnapshot;
  const arbol = { redirigir: true } as unknown as UrlTree;

  function ejecutar(guarda: CanActivateFn): unknown {
    return TestBed.runInInjectionContext(() => guarda({} as ActivatedRouteSnapshot, destino));
  }

  beforeEach(() => {
    auth = {
      isLoggedIn: vi.fn(() => true),
      esAnfitrion: vi.fn(() => true),
      logout: vi.fn(),
    };
    router = { createUrlTree: vi.fn(() => arbol) };
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  afterEach(() => vi.clearAllMocks());

  it('authGuard permite el paso con sesión activa', () => {
    expect(ejecutar(authGuard)).toBe(true);
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('authGuard cierra la sesión y redirige guardando la URL solicitada', () => {
    auth.isLoggedIn.mockReturnValue(false);

    expect(ejecutar(authGuard)).toBe(arbol);
    expect(auth.logout).toHaveBeenCalledWith(false);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/publicaciones' } });
  });

  it('invitadoGuard deja pasar a quien no tiene sesión', () => {
    auth.isLoggedIn.mockReturnValue(false);

    expect(ejecutar(invitadoGuard)).toBe(true);
  });

  it('invitadoGuard redirige al inicio a quien ya inició sesión', () => {
    expect(ejecutar(invitadoGuard)).toBe(arbol);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/']);
  });

  it('anfitrionGuard permite publicar sólo a anfitriones', () => {
    expect(ejecutar(anfitrionGuard)).toBe(true);

    auth.esAnfitrion.mockReturnValue(false);
    expect(ejecutar(anfitrionGuard)).toBe(arbol);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/']);
  });
});
