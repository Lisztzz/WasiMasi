package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** US15 - Participación autorizada y cursor de lectura dentro del grupo. */
@Entity
@Table(name = "miembros_grupo", uniqueConstraints = @UniqueConstraint(
        name = "uk_miembro_grupo", columnNames = {"vivienda_id", "usuario_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MiembroGrupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vivienda_id", nullable = false)
    private Vivienda vivienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private Long ultimaLecturaId;

    public MiembroGrupo(Vivienda vivienda, Usuario usuario) {
        this.vivienda = vivienda;
        this.usuario = usuario;
    }

    public void registrarLectura(Long mensajeId) {
        if (ultimaLecturaId == null || mensajeId > ultimaLecturaId) ultimaLecturaId = mensajeId;
    }
}
