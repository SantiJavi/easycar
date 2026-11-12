package com.auto.app.easycar.services;

import com.auto.app.easycar.entitis.RolVehiculo;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Punto unico de control de acceso a un vehiculo. Los servicios de dominio
 * (vehiculo, mantenimientos, historial) lo usan antes de leer o escribir.
 */
public interface AccesoVehiculoService {

    Usuario usuarioActual();

    /** Rol del usuario autenticado sobre el vehiculo, o null si no tiene acceso. */
    RolVehiculo rolDe(Vehiculo vehiculo);

    void exigirVer(Vehiculo vehiculo);

    void exigirEditar(Vehiculo vehiculo);

    void exigirPropietario(Vehiculo vehiculo);

    /** Vinculos del usuario autenticado (vehiculo + rol). */
    List<UserVehiculo> vinculosDelUsuarioActual();

    /** Ids de los vehiculos que el usuario autenticado puede consultar. */
    Set<UUID> vehiculosVisiblesIds();

    boolean puedeEditar(RolVehiculo rol);
}
