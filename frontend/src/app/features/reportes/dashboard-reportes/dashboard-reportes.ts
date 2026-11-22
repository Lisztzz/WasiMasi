import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';
import { ReportePrecio, ReporteZona } from '../../../core/models/reporte.model';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { ReporteService } from '../../../core/services/reporte.service';
import { mensajeDeError } from '../../../core/utils/api-error';

/** US20 - Panel de actividad de búsqueda, oferta e interés por visitas. */
@Component({
  selector: 'app-dashboard-reportes',
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule, TranslatePipe],
  templateUrl: './dashboard-reportes.html',
  styleUrl: './dashboard-reportes.scss',
})
export class DashboardReportes {
  private readonly api = inject(ReporteService);
  private readonly i18n = inject(I18nService);

  protected readonly zonas = signal<ReporteZona[]>([]);
  protected readonly rangos = signal<ReportePrecio[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly actualizado = signal<Date | null>(null);

  protected readonly totalBusquedas = computed(() => this.zonas().reduce((total, zona) => total + zona.busquedas, 0));
  protected readonly totalPublicaciones = computed(() => this.zonas().reduce((total, zona) => total + zona.publicacionesActivas, 0));
  protected readonly totalSolicitudes = computed(() => this.zonas().reduce((total, zona) => total + zona.solicitudesVisita, 0));
  protected readonly maxBusquedasZona = computed(() => Math.max(0, ...this.zonas().map((zona) => zona.busquedas)));
  protected readonly maxBusquedasRango = computed(() => Math.max(0, ...this.rangos().map((rango) => rango.busquedas)));

  constructor() {
    this.cargar();
  }

  /** US20 - Carga juntas las agregaciones de zona y presupuesto del backend. */
  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    forkJoin({ zonas: this.api.porZona(), rangos: this.api.porPrecio() }).subscribe({
      next: ({ zonas, rangos }) => {
        this.zonas.set(zonas);
        this.rangos.set(rangos);
        this.actualizado.set(new Date());
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, this.i18n));
        this.cargando.set(false);
      },
    });
  }

  porcentaje(valor: number, maximo: number): number {
    return maximo > 0 ? Math.max(4, (valor / maximo) * 100) : 0;
  }

  moneda(valor: number): string {
    return new Intl.NumberFormat(this.i18n.lang() === 'es' ? 'es-PE' : 'en-US', {
      style: 'currency', currency: 'PEN', maximumFractionDigits: 0,
    }).format(valor);
  }

  fechaActualizacion(): string {
    const fecha = this.actualizado();
    if (!fecha) return '';
    return fecha.toLocaleTimeString(this.i18n.lang() === 'es' ? 'es-PE' : 'en-US', {
      hour: '2-digit', minute: '2-digit',
    });
  }
}
