package com.auto.app.easycar.repositories;

import com.auto.app.easycar.entitis.InvitacionVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitacionVehiculoRepository extends JpaRepository<InvitacionVehiculo, Long> {

    Optional<InvitacionVehiculo> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigo(String codigo);

    List<InvitacionVehiculo> findByFkVehiculoInvitacion_IdOrderByFechaCreacionDesc(UUID vehiculoId);
}
