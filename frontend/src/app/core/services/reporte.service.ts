import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReportePrecio, ReporteZona } from '../models/reporte.model';

/** Indicadores de demanda y oferta para US20. */
@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/reportes`;

  porZona(): Observable<ReporteZona[]> {
    return this.http.get<ReporteZona[]>(`${this.api}/zonas`);
  }

  porPrecio(): Observable<ReportePrecio[]> {
    return this.http.get<ReportePrecio[]>(`${this.api}/precios`);
  }
}
