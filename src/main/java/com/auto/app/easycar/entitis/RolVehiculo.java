package com.auto.app.easycar.entitis;

/**
 * Rol de un usuario sobre un vehiculo.
 *  - PROPIETARIO: creo el vehiculo; puede borrarlo y gestionar accesos.
 *  - COLABORADOR: puede ver, actualizar kilometraje y gestionar mantenimientos/historial.
 *  - LECTURA: solo consulta; no puede modificar nada.
 */
public enum RolVehiculo {
    PROPIETARIO,
    COLABORADOR,
    LECTURA
}
