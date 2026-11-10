package com.auto.app.easycar.entitis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Serializacion (anti-recursion):
 *  - Vehiculo -> historial y Usuario -> historial son visibles.
 *  - Historial -> vehiculo / usuario / tipo de mantenimiento: ignorados (no viceversa).
 */
@Entity
@Table(name = "historial")
@Getter
@Setter
public class HistorialMantenimientos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date fechaMantenimiento;
    private Integer kilometrajeActual;
    private String observacion;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "fk_tipo_historial_id", nullable = false)
    private TipoMantenimiento fkTipoHistorial;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "fk_user_historial_id", nullable = false)
    private Usuario fkUserHistorial;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "fk_vehiculo_historial_id", nullable = false)
    private Vehiculo fkVehiculoHistorial;
}
