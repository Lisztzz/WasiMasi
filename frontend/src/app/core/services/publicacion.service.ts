import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FiltrosBusqueda, Publicacion, PublicacionRequest } from '../models/publicacion.model';
import { normalizarZona } from '../utils/distritos';

/** Publicaciones de habitaciones (US06 publicar, US08 buscar, US11 detalle). */
@Injectable({ providedIn: 'root' })
export class PublicacionService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/publicaciones`;

  /** Última búsqueda, para volver a los resultados desde el detalle. */
  readonly ultimaBusqueda = signal<FiltrosBusqueda | null>(null);

  /** GET /publicaciones?zona&precioMin&precioMax (solo publicaciones ACTIVA). */
  buscar(filtros: FiltrosBusqueda): Observable<Publicacion[]> {
    this.ultimaBusqueda.set(filtros);
    const params = new HttpParams()
      .set('zona', normalizarZona(filtros.zona))
      .set('precioMin', filtros.precioMin)
      .set('precioMax', filtros.precioMax);
    return this.http.get<Publicacion[]>(this.api, { params });
  }

  /** GET /publicaciones/{id} */
  obtener(id: number): Observable<Publicacion> {
    return this.http.get<Publicacion>(`${this.api}/${id}`);
  }

  /** POST /publicaciones (solo ANFITRION; el anfitrión sale del JWT). */
  publicar(datos: PublicacionRequest): Observable<Publicacion> {
    return this.http.post<Publicacion>(this.api, { ...datos, zona: normalizarZona(datos.zona) });
  }
}
