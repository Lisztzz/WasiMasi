import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, effect, inject, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService } from '../../../core/i18n/i18n.service';
import { Publicacion } from '../../../core/models/publicacion.model';
import * as L from 'leaflet';

/** US10 - Mapa de resultados con Leaflet + OpenStreetMap (sin API key). */
@Component({
  selector: 'app-mapa-resultados',
  imports: [MatIconModule, TranslatePipe],
  templateUrl: './mapa-resultados.html',
  styleUrl: './mapa-resultados.scss',
})
export class MapaResultados implements AfterViewInit, OnDestroy {
  /** Publicaciones devueltas por la búsqueda; solo se pintan las que tienen coordenadas. */
  readonly publicaciones = input<Publicacion[]>([]);

  private readonly i18n = inject(I18nService);
  @ViewChild('lienzo', { static: true }) private lienzo!: ElementRef<HTMLElement>;

  private mapa: L.Map | null = null;
  private marcadores: L.LayerGroup | null = null;

  /** Centro por defecto: Lima. */
  private static readonly CENTRO: L.LatLngExpression = [-12.0464, -77.0428];

  constructor() {
    // Al cambiar el idioma se redibujan los popup con el texto traducido.
    effect(() => {
      this.publicaciones();
      this.i18n.lang();
      this.pintarMarcadores();
    });
  }

  ngAfterViewInit(): void {
    this.mapa = L.map(this.lienzo.nativeElement, { scrollWheelZoom: false })
      .setView(MapaResultados.CENTRO, 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(this.mapa);

    this.marcadores = L.layerGroup().addTo(this.mapa);
    this.pintarMarcadores();
    // El panel puede crearse oculto (pestaña "Mapa"): Leaflet necesita recalcular su tamaño.
    setTimeout(() => this.mapa?.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.mapa?.remove();
    this.mapa = null;
  }

  /** Coloca un marcador por publicación con coordenadas y ajusta el encuadre. */
  private pintarMarcadores(): void {
    if (!this.mapa || !this.marcadores) {
      return;
    }
    this.marcadores.clearLayers();

    const puntos: L.LatLngTuple[] = [];
    for (const publicacion of this.publicaciones()) {
      const lat = Number(publicacion.latitud);
      const lon = Number(publicacion.longitud);
      if (publicacion.latitud == null || publicacion.longitud == null) {
        continue;
      }
      if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
        continue;
      }
      puntos.push([lat, lon]);
      // CircleMarker es SVG: evita las imágenes de los íconos por defecto de Leaflet,
      // que no se resuelven al empaquetar la app con Angular.
      L.circleMarker([lat, lon], {
        radius: 9,
        color: '#0f2d40',
        weight: 2,
        fillColor: '#c4532f',
        fillOpacity: 0.9,
      }).bindPopup(this.contenidoPopup(publicacion)).addTo(this.marcadores);
    }

    if (puntos.length) {
      this.mapa.fitBounds(L.latLngBounds(puntos).pad(0.2));
    } else {
      this.mapa.setView(MapaResultados.CENTRO, 12);
    }
  }

  /** El contenido del popup se crea como DOM (nunca HTML interpolado) para evitar XSS. */
  private contenidoPopup(publicacion: Publicacion): HTMLElement {
    const contenido = document.createElement('div');

    const titulo = document.createElement('strong');
    titulo.textContent = this.i18n.t('detalle.titulo', { zona: publicacion.zona });

    const precio = document.createElement('div');
    precio.textContent = `S/ ${publicacion.precio}`;

    contenido.append(titulo, precio);
    return contenido;
  }
}