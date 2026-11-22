import { Component, DestroyRef, computed, effect, ElementRef, inject, input, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DateAdapter, MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Publicacion } from '../../../core/models/publicacion.model';
import { AuthService } from '../../../core/services/auth.service';
import { ConversacionService } from '../../../core/services/conversacion.service';
import { PublicacionService } from '../../../core/services/publicacion.service';
import { VisitaService } from '../../../core/services/visita.service';
import { mensajeDeError } from '../../../core/utils/api-error';
import {
  esCondicionNegociable, textoCoordenadas, textoGenero, textoMascotas, textoServicios,
} from '../../../core/utils/condiciones-publicacion';
import { MapaResultados } from '../../../shared/components/mapa-resultados/mapa-resultados';
import { SelloVerificacion } from '../../../shared/components/sello-verificacion/sello-verificacion';
import { iniciales } from '../../../shared/layout/main-layout/main-layout';
import { ContactoDialog } from '../../mensajes/contacto/contacto-dialog';
import { VincularVivienda } from './vincular-vivienda';

/** US11 - Detalle de una habitación (mock-up 04). */
@Component({
  selector: 'app-detalle',
  imports: [
    DecimalPipe, ReactiveFormsModule, RouterLink, MatButtonModule, MatDialogModule, MatFormFieldModule,
    MatDatepickerModule, MatNativeDateModule, MatIconModule, MatInputModule,
    MatProgressSpinnerModule, TranslatePipe, MapaResultados, SelloVerificacion, VincularVivienda,
  ],
  templateUrl: './detalle.html',
  styleUrl: './detalle.scss',
})
export class Detalle {
  private readonly publicaciones = inject(PublicacionService);
  private readonly conversaciones = inject(ConversacionService);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly visitas = inject(VisitaService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly i18n = inject(I18nService);
  private readonly dateAdapter = inject<DateAdapter<Date>>(DateAdapter);
  private readonly snack = inject(MatSnackBar);

  /** Parámetro :id de la ruta /publicaciones/:id */
  readonly id = input.required<string>();

  protected readonly publicacion = signal<Publicacion | null>(null);
  protected readonly esPropietario = computed(() => this.publicacion()?.anfitrionId === this.auth.usuario()?.id);

  protected actualizarVivienda(viviendaId: number): void {
    this.publicacion.update((publicacion) => publicacion && { ...publicacion, viviendaId });
  }
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly fotoActual = signal(0);
  protected readonly contactando = signal(false);
  protected readonly errorContacto = signal<string | null>(null);
  protected readonly puedeContactar = computed(() =>
    this.auth.usuario()?.rol === 'BUSCADOR' && this.publicacion()?.estado === 'ACTIVA',
  );
  protected readonly fotosFallidas = signal<ReadonlySet<number>>(new Set());
  protected readonly mostrarFormularioVisita = signal(false);
  protected readonly enviandoVisita = signal(false);
  protected readonly errorVisita = signal<string | null>(null);
  protected readonly selectorHoraAbierto = signal(false);
  protected readonly horaElegida24 = signal('');
  protected readonly horaReloj = signal(7);
  protected readonly minutoReloj = signal(0);
  protected readonly periodoReloj = signal<'AM' | 'PM'>('AM');
  protected readonly horasReloj = computed(() => this.periodoReloj() === 'AM'
    ? [7, 8, 9, 10, 11]
    : [12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);
  protected readonly minutosReloj = Array.from({ length: 180 }, (_, indice) => ({ indice, valor: indice % 60 }));
  private readonly listaMinutos = viewChild<ElementRef<HTMLDivElement>>('listaMinutos');
  protected readonly horaFormateada = computed(() => {
    const valor = this.horaElegida24();
    if (!valor) return '';
    const [hora, minuto] = valor.split(':').map(Number);
    return this.formatearHora(hora, minuto);
  });
  protected readonly horaEnSelector = computed(() => {
    const hora24 = this.periodoReloj() === 'PM' ? (this.horaReloj() % 12) + 12 : this.horaReloj() % 12;
    return this.formatearHora(hora24, this.minutoReloj());
  });
  protected readonly visitaForm = this.fb.group({
    fecha: this.fb.control<Date | null>(null, Validators.required),
    hora: ['', Validators.required],
    comentario: ['', Validators.maxLength(500)],
  });
  protected readonly puedeSolicitarVisita = computed(
    () => this.auth.usuario()?.rol === 'BUSCADOR'
      && this.auth.usuario()?.id !== this.publicacion()?.anfitrionId
      && this.publicacion()?.estado === 'ACTIVA',
  );
  /** Para "Volver a resultados" con los mismos filtros. */
  protected readonly volverParams = computed(() => this.publicaciones.ultimaBusqueda() ?? {});
  protected readonly inicialesAnfitrion = computed(() => iniciales(this.publicacion()?.anfitrionNombre));

  /** US09/US10 - Condiciones y ubicación de la habitación. */
  protected readonly mascotasTexto = computed(() => {
    const p = this.publicacion();
    return p ? textoMascotas(p, this.i18n) : null;
  });
  protected readonly generoTexto = computed(() => {
    const p = this.publicacion();
    return p ? textoGenero(p, this.i18n) : null;
  });
  protected readonly serviciosTexto = computed(() => {
    const p = this.publicacion();
    return p ? textoServicios(p) : null;
  });
  /** US09 - Etiqueta "Negociable" en las condiciones que el anfitrión acepta cambiar. */
  protected readonly mascotasNegociable = computed(() => {
    const p = this.publicacion();
    return p ? esCondicionNegociable(p, 'mascotas') : false;
  });
  protected readonly generoNegociable = computed(() => {
    const p = this.publicacion();
    return p ? esCondicionNegociable(p, 'genero') : false;
  });
  protected readonly serviciosNegociable = computed(() => {
    const p = this.publicacion();
    return p ? esCondicionNegociable(p, 'servicios') : false;
  });
  protected readonly coordenadasTexto = computed(() => {
    const p = this.publicacion();
    return p ? textoCoordenadas(p, this.i18n) : null;
  });
  /** El mapa del detalle muestra únicamente esta publicación. */
  protected readonly mapaPublicacion = computed(() => {
    const p = this.publicacion();
    return p?.latitud != null && p.longitud != null ? [p] : [];
  });

  protected readonly fecha = computed(() => {
    const p = this.publicacion();
    if (!p?.fechaPublicacion) return '';
    return new Date(p.fechaPublicacion).toLocaleDateString(this.i18n.lang() === 'es' ? 'es-PE' : 'en-US', {
      day: 'numeric', month: 'long', year: 'numeric',
    });
  });

  constructor() {
    effect(() => this.dateAdapter.setLocale(this.i18n.lang() === 'es' ? 'es-PE' : 'en-US'));
    effect(() => this.cargar(Number(this.id())));
  }

  private cargar(id: number): void {
    this.cargando.set(true);
    this.error.set(null);
    this.errorContacto.set(null);
    this.errorVisita.set(null);
    this.mostrarFormularioVisita.set(false);
    this.selectorHoraAbierto.set(false);
    this.horaElegida24.set('');
    this.visitaForm.reset({ fecha: null, hora: '', comentario: '' });
    this.fotoActual.set(0);
    this.publicaciones
      .obtener(id)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (p) => this.publicacion.set(p),
        error: (err) => this.error.set(mensajeDeError(err, this.i18n)),
      });
  }

  marcarFotoFallida(indice: number): void {
    this.fotosFallidas.update((s) => new Set(s).add(indice));
  }

  /** US12 - Reutiliza la conversación abierta o pide el primer mensaje. */
  contactar(publicacion: Publicacion): void {
    if (this.contactando()) return;
    this.contactando.set(true);
    this.errorContacto.set(null);
    this.conversaciones.consultarContacto(publicacion.id)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.contactando.set(false)))
      .subscribe({
        next: ({ conversacionId }) => {
          if (Number(this.id()) !== publicacion.id) return;
          if (conversacionId !== null) {
            this.router.navigate(['/mensajes', conversacionId]);
            return;
          }
          this.dialog.open<ContactoDialog, Publicacion, number>(ContactoDialog, {
            data: publicacion,
            width: '480px',
          }).afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe((id) => {
            if (id !== undefined && Number(this.id()) === publicacion.id) {
              this.router.navigate(['/mensajes', id]);
            }
          });
        },
        error: (err) => {
          if (Number(this.id()) === publicacion.id) {
            this.errorContacto.set(mensajeDeError(err, this.i18n));
          }
        },
      });
  }

  /** US16 - Abre o cierra el formulario para solicitar visita. */
  alternarFormularioVisita(): void {
    this.errorVisita.set(null);
    this.selectorHoraAbierto.set(false);
    this.mostrarFormularioVisita.update((visible) => !visible);
  }

  /** US16 - Abre el selector visual de hora al tocar el campo. */
  abrirSelectorHora(): void {
    this.errorVisita.set(null);
    const valor = this.horaElegida24();
    if (valor) {
      const [hora24, minuto] = valor.split(':').map(Number);
      this.periodoReloj.set(hora24 >= 12 ? 'PM' : 'AM');
      this.horaReloj.set(hora24 % 12 || 12);
      this.minutoReloj.set(minuto);
    } else {
      this.periodoReloj.set('AM');
      this.horaReloj.set(7);
      this.minutoReloj.set(0);
    }
    this.selectorHoraAbierto.set(true);
    requestAnimationFrame(() => this.posicionarRuedaMinutos());
  }

  /** US16 - Mantiene una hora admitida al cambiar entre la mañana y la tarde. */
  cambiarPeriodo(periodo: 'AM' | 'PM'): void {
    this.periodoReloj.set(periodo);
    if (!this.horasReloj().includes(this.horaReloj())) {
      this.horaReloj.set(periodo === 'AM' ? 7 : 12);
    }
  }

  /** US16 - Selecciona una hora en la rueda visual. */
  elegirHoraReloj(hora: number): void {
    this.horaReloj.set(hora);
  }

  /** US16 - Selecciona cualquier minuto entre 00 y 59. */
  elegirMinutoReloj(minuto: number): void {
    this.minutoReloj.set(minuto);
  }

  /** US16 - Mantiene el selector de minutos en una rueda continua del 00 al 59. */
  ajustarBucleMinutos(event: Event): void {
    const lista = event.currentTarget as HTMLDivElement;
    const vuelta = lista.scrollHeight / 3;
    if (lista.scrollTop < vuelta / 2) {
      lista.scrollTop += vuelta;
    } else if (lista.scrollTop > vuelta * 2.5) {
      lista.scrollTop -= vuelta;
    }
  }

  /** US16 - Guarda la hora elegida en el formulario. */
  confirmarHora(): void {
    const hora24 = this.periodoReloj() === 'PM' ? (this.horaReloj() % 12) + 12 : this.horaReloj() % 12;
    const valor = `${hora24.toString().padStart(2, '0')}:${this.minutoReloj().toString().padStart(2, '0')}`;
    this.horaElegida24.set(valor);
    this.visitaForm.controls.hora.setValue(valor);
    this.visitaForm.controls.hora.markAsTouched();
    this.errorVisita.set(null);
    this.selectorHoraAbierto.set(false);
  }

  /** US16 - Cierra el selector sin aplicar los cambios temporales. */
  cancelarSelectorHora(): void {
    this.selectorHoraAbierto.set(false);
  }

  /** US16 - Muestra horas y minutos con dos dígitos. */
  formatearDosDigitos(valor: number): string {
    return valor.toString().padStart(2, '0');
  }

  private formatearHora(hora: number, minuto: number): string {
    const locale = this.i18n.lang() === 'es' ? 'es-PE' : 'en-US';
    return new Intl.DateTimeFormat(locale, { hour: 'numeric', minute: '2-digit' })
      .format(new Date(2020, 0, 1, hora, minuto));
  }

  private posicionarRuedaMinutos(): void {
    const lista = this.listaMinutos()?.nativeElement;
    if (!lista) return;
    const botones = lista.querySelectorAll<HTMLButtonElement>('.selector-hora__opcion');
    const seleccionado = botones.item(60 + this.minutoReloj());
    if (seleccionado) {
      lista.scrollTop = seleccionado.offsetTop - (lista.clientHeight - seleccionado.offsetHeight) / 2;
    }
  }

  /** US16 - Envía la fecha solicitada junto con la publicación actual. */
  solicitarVisita(publicacionId: number): void {
    this.visitaForm.markAllAsTouched();
    if (this.visitaForm.invalid) return;

    const fecha = this.visitaForm.controls.fecha.value;
    const hora = this.visitaForm.controls.hora.value;
    if (!fecha || !hora) return;

    const [horas, minutos] = hora.split(':').map(Number);
    const minutosDelDia = horas * 60 + minutos;
    if (minutosDelDia < 7 * 60 || minutosDelDia > 22 * 60 + 59) {
      this.errorVisita.set(this.i18n.t('visitas.error.horarioNoDisponible'));
      return;
    }
    const fechaHoraLocal = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate(), horas, minutos);
    if (fechaHoraLocal.getTime() <= Date.now()) {
      this.errorVisita.set(this.i18n.t('visitas.error.fechaFutura'));
      return;
    }

    const fechaLocal = [
      fecha.getFullYear().toString().padStart(4, '0'),
      (fecha.getMonth() + 1).toString().padStart(2, '0'),
      fecha.getDate().toString().padStart(2, '0'),
    ].join('-');
    const fechaHora = `${fechaLocal}T${hora}:00`;

    this.enviandoVisita.set(true);
    this.errorVisita.set(null);
    this.visitas.solicitar({
      publicacionId,
      fechaHora,
      comentario: this.visitaForm.controls.comentario.value.trim() || null,
    }).pipe(finalize(() => this.enviandoVisita.set(false))).subscribe({
      next: () => {
        this.mostrarFormularioVisita.set(false);
        this.selectorHoraAbierto.set(false);
        this.horaElegida24.set('');
        this.visitaForm.reset({ fecha: null, hora: '', comentario: '' });
        this.snack.open(this.i18n.t('visitas.solicitudExitosa'), this.i18n.t('comun.cerrar'), { duration: 5000 });
      },
      error: (err) => this.errorVisita.set(mensajeDeError(err, this.i18n)),
    });
  }

  /** US16 - La fecha mínima es hoy en la zona horaria local. */
  fechaMinima(): Date {
    const ahora = new Date();
    return new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate());
  }
}
