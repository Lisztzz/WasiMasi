import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/**
 * Agrega "Authorization: Bearer <token>" a cada petición hacia la API.
 * Si la API responde 401 (token vencido o inválido), cierra la sesión y lleva al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const esApi = req.url.startsWith(environment.apiUrl);
  const esAuth = req.url.startsWith(`${environment.apiUrl}/auth/`);
  const token = auth.token();

  const peticion =
    esApi && !esAuth && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && esApi && !esAuth) {
        auth.logout(false);
        router.navigate(['/login'], { queryParams: { expirada: 1 } });
      }
      return throwError(() => error);
    }),
  );
};
