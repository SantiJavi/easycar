package com.auto.app.easycar.entitis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Invitacion para compartir un vehiculo.
 * El dueño genera un codigo, elige cuantos dias dura y puede revocarlo o
 * renovarlo (extender la vigencia) sin generar uno nuevo.
 */
@Entity
@Table(name = "invitacion_vehiculo")
@Getter
@Setter
public class InvitacionVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 12)
    private String codigo;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_invitacion_vehiculo_id", nullable = false)
    private Vehiculo fkVehiculoInvitacion;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_invitacion_usuario_id", nullable = false)
    private Usuario fkUsuarioInvitacion;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaCreacion;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiraEn;
    @Column(nullable = false)
    private boolean revocada = false;
}
