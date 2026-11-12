package com.auto.app.easycar.entitis;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name ="user_vehiculo")
@Getter
@Setter
public class UserVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "fk_user_vehiculo_id", nullable = false)
    private Usuario fkUserVehiculo;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "fk_vehiculo_user_id", nullable = false)
    private Vehiculo fkVehiculoUser;
    /**
     * Rol del usuario sobre el vehiculo. El default a nivel de columna cubre las
     * filas que ya existian antes del acceso compartido: su dueño original queda
     * como PROPIETARIO al aplicar el cambio con ddl-auto=update.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolVehiculo rol = RolVehiculo.PROPIETARIO;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAlta;

}
