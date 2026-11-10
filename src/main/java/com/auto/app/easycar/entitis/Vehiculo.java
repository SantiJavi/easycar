package com.auto.app.easycar.entitis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Serializacion (anti-recursion):
 *  - Vehiculo -> tipos de mantenimiento: visible.
 *  - Vehiculo -> historial: visible.
 *  - Vehiculo -> user_vehiculo: ignorado (UserVehiculo -> Vehiculo volveria a entrar aqui).
 */
@Entity
@Table(name = "vehiculo")
@Getter
@Setter
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String placa;
    private String nombre;
    @Column(nullable = false)
    private String marca;
    private LocalDate fechaMatricula;
    /** Kilometraje actual declarado por el usuario (se actualiza sin crear historial). */
    @Column(name = "kilometraje_actual")
    private Integer kilometrajeActual;
    @JsonIgnore
    @OneToMany(mappedBy = "fkVehiculoUser")
    private List<UserVehiculo> fkVehiculoUser;
    @OneToMany(mappedBy = "fkTipoMantenimiento")
    private List<TipoMantenimiento> fkTipoMantenimiento;
    @OneToMany(mappedBy = "fkVehiculoHistorial")
    private List<HistorialMantenimientos> fkVehiculoHistorial;

}
