package com.auto.app.easycar.entitis;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

}
