package com.auto.app.easycar.repositories;

import com.auto.app.easycar.entitis.TipoMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TipoMantenimientoRepository extends JpaRepository<TipoMantenimiento, Long> {

    List<TipoMantenimiento> findByFkTipoMantenimiento_Id(UUID vehiculoId);
}
