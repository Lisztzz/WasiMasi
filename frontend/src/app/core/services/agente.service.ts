import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AccionAgente, ConsultaAgente, RespuestaAgente } from '../models/agente.model';

/** US21/US22 - Agente de IA de WasiMasi (Spring AI en el backend). */
@Injectable({ providedIn: 'root' })
export class AgenteService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/agente`;

  /** POST /agente/consultas */
  consultar(consulta: ConsultaAgente): Observable<RespuestaAgente> {
    return this.http.post<RespuestaAgente>(`${this.api}/consultas`, consulta);
  }

  /** POST /agente/acciones/{id}/confirmar (solo BUSCADOR): recién aquí se crea la visita. */
  confirmar(id: number): Observable<AccionAgente> {
    return this.http.post<AccionAgente>(`${this.api}/acciones/${id}/confirmar`, {});
  }

  /** POST /agente/acciones/{id}/cancelar */
  cancelar(id: number): Observable<AccionAgente> {
    return this.http.post<AccionAgente>(`${this.api}/acciones/${id}/cancelar`, {});
  }
}
