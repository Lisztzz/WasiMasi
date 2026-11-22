import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Interesado, Visita, VisitaRequest, VisitaRespuestaRequest } from '../models/visita.model';

/** Solicitudes de visita, respuestas del anfitrión e interesados. */
@Injectable({ providedIn: 'root' })
export class VisitaService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/visitas`;

  /** US16 - Solicitar una visita como buscador. */
  solicitar(request: VisitaRequest): Observable<Visita> {
    return this.http.post<Visita>(this.api, request);
  }

  /** US17 - Visitas recibidas por el anfitrión autenticado. */
  misVisitas(): Observable<Visita[]> {
    return this.http.get<Visita[]>(`${this.api}/mis-visitas`);
  }

  /** US17 - Confirmar o rechazar una solicitud. */
  responder(id: number, request: VisitaRespuestaRequest): Observable<Visita> {
    return this.http.patch<Visita>(`${this.api}/${id}/respuesta`, request);
  }

  /** US19 - Comparar a quienes solicitaron visita para una publicación propia. */
  interesados(publicacionId: number): Observable<Interesado[]> {
    return this.http.get<Interesado[]>(`${this.api}/publicacion/${publicacionId}/interesados`);
  }
}
