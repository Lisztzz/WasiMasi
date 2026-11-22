import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ContactoResponse, ConversacionResumen, DetalleConversacion, Mensaje, MensajesNoLeidos } from '../models/conversacion.model';

/** US12/US13 - Primer contacto, historial y envíos del chat. */
@Injectable({ providedIn: 'root' })
export class ConversacionService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  consultarContacto(publicacionId: number): Observable<ContactoResponse> {
    return this.http.get<ContactoResponse>(`${this.api}/publicaciones/${publicacionId}/contacto`);
  }

  iniciarContacto(publicacionId: number, mensaje: string): Observable<{ conversacionId: number }> {
    return this.http.post<{ conversacionId: number }>(
      `${this.api}/publicaciones/${publicacionId}/contacto`,
      { mensaje },
    );
  }

  listar(): Observable<ConversacionResumen[]> {
    return forkJoin({
      directas: this.http.get<ConversacionResumen[]>(`${this.api}/conversaciones`),
      grupos: this.http.get<ConversacionResumen[]>(`${this.api}/grupos`),
    }).pipe(map(({ directas, grupos }) =>
      [...directas, ...grupos.map((grupo) => ({ ...grupo, grupal: true }))]
        .sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion)),
    ));
  }

  obtener(id: number, grupal: boolean): Observable<DetalleConversacion> {
    return this.http.get<DetalleConversacion>(this.urlChat(id, grupal), { timeout: 7000 });
  }

  nuevosMensajes(id: number, despuesDe: number, grupal: boolean): Observable<{ mensajes: Mensaje[]; cerrada?: boolean }> {
    const params = new HttpParams().set('despuesDe', despuesDe);
    const url = this.urlChat(id, grupal) + '/mensajes';
    const opciones = { params, timeout: 7000 };
    return grupal
      ? this.http.get<DetalleConversacion>(url, opciones).pipe(
        map(({ conversacion, mensajes }) => ({ mensajes, cerrada: conversacion.cerrada })),
      )
      : this.http.get<Mensaje[]>(url, opciones).pipe(map((mensajes) => ({ mensajes })));
  }

  enviar(id: number, clienteId: string, contenido: string, grupal: boolean): Observable<Mensaje> {
    return this.http.post<Mensaje>(this.urlChat(id, grupal) + '/mensajes',
      { clienteId, contenido }, { timeout: 7000 });
  }

  marcarLectura(id: number, mensajeId: number, grupal: boolean): Observable<void> {
    return this.http.post<void>(this.urlChat(id, grupal) + '/lectura', { mensajeId }, { timeout: 7000 });
  }

  consultarNoLeidos(): Observable<MensajesNoLeidos> {
    return this.http.get<MensajesNoLeidos>(`${this.api}/conversaciones/no-leidos`, { timeout: 7000 });
  }

  private urlChat(id: number, grupal: boolean): string {
    return `${this.api}/${grupal ? 'grupos' : 'conversaciones'}/${id}`;
  }
}

export function esErrorTemporal(error: unknown): boolean {
  return error instanceof HttpErrorResponse &&
    (error.status === 0 || error.status === 408 || error.status === 429 || error.status >= 500);
}
