import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EditarPublicacionRequest, EstadoGestionPublicacion, FiltrosBusqueda, Publicacion, PublicacionRequest,
} from '../models/publicacion.model';
import { normalizarZona } from '../utils/distritos';

/** Publicaciones de habitaciones (US06, US07, US08, US09, US11, US18 y US20). */
@Injectable({ providedIn: 'root' })
export class PublicacionService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/publicaciones`;

  /** Última búsqueda, para volver a los resultados desde el detalle. */
  readonly ultimaBusqueda = signal<FiltrosBusqueda | null>(null);

  /** US08/US09/US20 - Busca habitaciones y registra los filtros anónimos para los reportes. */
  buscar(filtros: FiltrosBusqueda): Observable<Publicacion[]> {
    this.ultimaBusqueda.set(filtros);
    return this.http.post<Publicacion[]>(`${this.api}/buscar`, {
      zona: normalizarZona(filtros.zona),
      precioMin: filtros.precioMin,
      precioMax: filtros.precioMax,
      mascotas: filtros.mascotas,
      genero: filtros.genero || undefined,
      servicios: filtros.servicios || undefined,
    });
  }

  /**
   * US08/US09 - Recupera una búsqueda ya presente en la URL sin volver a registrarla en US20.
   * GET /publicaciones?zona&precioMin&precioMax [&mascotas&genero&servicios]; los filtros
   * de US09 solo se envían cuando el usuario los eligió.
   */
  consultar(filtros: FiltrosBusqueda): Observable<Publicacion[]> {
    this.ultimaBusqueda.set(filtros);
    let params = new HttpParams()
      .set('zona', normalizarZona(filtros.zona))
      .set('precioMin', String(filtros.precioMin))
      .set('precioMax', String(filtros.precioMax));

    if (filtros.mascotas !== undefined) {
      params = params.set('mascotas', filtros.mascotas);
    }
    if (filtros.genero) {
      params = params.set('genero', filtros.genero);
    }
    if (filtros.servicios) {
      params = params.set('servicios', filtros.servicios);
    }
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

  /** US07 - Publicaciones del anfitrión autenticado. */
  listarMias(): Observable<Publicacion[]> {
    return this.http.get<Publicacion[]>(`${this.api}/mias`);
  }

  /** US07 - Guardar los datos de una publicación propia. */
  editar(id: number, datos: EditarPublicacionRequest): Observable<Publicacion> {
    return this.http.put<Publicacion>(`${this.api}/${id}`, { ...datos, zona: normalizarZona(datos.zona) });
  }

  /** US07 y US18 - Cambiar el estado de una publicación propia. */
  cambiarEstado(id: number, estado: EstadoGestionPublicacion): Observable<Publicacion> {
    return this.http.patch<Publicacion>(`${this.api}/${id}/estado`, { estado });
  }
}
