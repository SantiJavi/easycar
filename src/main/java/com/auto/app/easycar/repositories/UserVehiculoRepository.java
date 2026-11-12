package com.auto.app.easycar.repositories;

import com.auto.app.easycar.entitis.UserVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVehiculoRepository extends JpaRepository<UserVehiculo, Long> {

    List<UserVehiculo> findByFkUserVehiculo_Id(UUID usuarioId);

    List<UserVehiculo> findByFkVehiculoUser_Id(UUID vehiculoId);

    Optional<UserVehiculo> findByFkUserVehiculo_IdAndFkVehiculoUser_Id(UUID usuarioId, UUID vehiculoId);

    boolean existsByFkUserVehiculo_IdAndFkVehiculoUser_Id(UUID usuarioId, UUID vehiculoId);
}
