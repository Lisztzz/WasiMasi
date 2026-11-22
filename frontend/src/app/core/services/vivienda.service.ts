import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Vivienda } from '../models/publicacion.model';

/** US15 - Gestión de viviendas y asignación de habitaciones. */
@Injectable({ providedIn: 'root' })
export class ViviendaService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  listar() {
    return this.http.get<Vivienda[]>(`${this.api}/viviendas`);
  }

  crear(nombre: string) {
    return this.http.post<Vivienda>(`${this.api}/viviendas`, { nombre });
  }

  vincular(publicacionId: number, viviendaId: number) {
    return this.http.put<Vivienda>(`${this.api}/publicaciones/${publicacionId}/vivienda`, { viviendaId });
  }
}
