export interface ReporteZona {
  zona: string;
  busquedas: number;
  publicacionesActivas: number;
  solicitudesVisita: number;
}

/** Agrupación por el presupuesto exacto solicitado en búsquedas reales. */
export interface ReportePrecio {
  precioMin: number;
  precioMax: number;
  busquedas: number;
  publicacionesActivas: number;
}
