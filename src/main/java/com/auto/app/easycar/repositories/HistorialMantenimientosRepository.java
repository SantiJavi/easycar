package com.auto.app.easycar.repositories;

import com.auto.app.easycar.entitis.HistorialMantenimientos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialMantenimientosRepository extends JpaRepository<HistorialMantenimientos, Long> {

    List<HistorialMantenimientos> findByFkVehiculoHistorial_Id(UUID vehiculoId);

    List<HistorialMantenimientos> findByFkUserHistorial_Id(UUID usuarioId);

    List<HistorialMantenimientos> findByFkTipoHistorial_Id(Long tipoMantenimientoId);

    List<HistorialMantenimientos> findByFkVehiculoHistorial_IdIn(java.util.Collection<UUID> vehiculoIds);
}
