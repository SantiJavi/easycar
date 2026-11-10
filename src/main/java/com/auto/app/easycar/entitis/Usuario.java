package com.auto.app.easycar.entitis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Serializacion (anti-recursion):
 *  - Usuario -> historial: visible.
 *  - Usuario -> user_vehiculo: ignorado (UserVehiculo -> Usuario volveria a entrar aqui).
 * La API usa DTOs; estas anotaciones protegen el caso de serializar la entidad directamente.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(length = 100, nullable = false)
    private String usuario;
    @Column(length = 100,unique = true,nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @JsonIgnore
    @OneToMany(mappedBy = "fkUserVehiculo")
    List<UserVehiculo> fkUserVehiculo;
    @OneToMany(mappedBy = "fkUserHistorial")
    List<HistorialMantenimientos> fkUserHistorial;

}
