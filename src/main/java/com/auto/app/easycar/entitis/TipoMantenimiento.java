package com.auto.app.easycar.entitis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Serializacion (anti-recursion):
 *  - Vehiculo -> tipo de mantenimiento: visible desde el vehiculo.
 *  - TipoMantenimiento -> vehiculo: ignorado (no viceversa).
 *  - TipoMantenimiento -> historial: ignorado (el historial tiene su propio endpoint).
 */
@Entity
@Table(name="tipo_mantenimiento")
@Getter
@Setter
public class TipoMantenimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombreMantenimiento;
    private Date fechaSiguienteMantenimiento;
    @Column(length = 255)
    private String observacion;
    private Integer kilometrajeObjetivo;
    /** Cada cuantos kilometros corresponde el mantenimiento (tipoVista = 1). */
    private Integer frecuenciaKm;
    /** Cada cuantos meses corresponde el mantenimiento (tipoVista = 2). */
    private Integer frecuenciaMeses;
    @Column(length = 2,nullable = false)
    private Integer tipoVista;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "fk_tipo_vehiculo_id", nullable = false)
    private Vehiculo fkTipoMantenimiento;
    @JsonIgnore
    @OneToMany(mappedBy = "fkTipoHistorial")
    private List<HistorialMantenimientos> fkTipoHistorial;

}
