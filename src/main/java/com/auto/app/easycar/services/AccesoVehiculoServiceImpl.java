package com.auto.app.easycar.services;

import com.auto.app.easycar.entitis.RolVehiculo;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import com.auto.app.easycar.repositories.UserVehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccesoVehiculoServiceImpl implements AccesoVehiculoService {

    private final UserVehiculoRepository userVehiculoRepository;
    private final CurrentUserService currentUserService;

    public AccesoVehiculoServiceImpl(UserVehiculoRepository userVehiculoRepository,
                                     CurrentUserService currentUserService) {
        this.userVehiculoRepository = userVehiculoRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public Usuario usuarioActual() {
        return currentUserService.actual();
    }

    @Override
    @Transactional(readOnly = true)
    public RolVehiculo rolDe(Vehiculo vehiculo) {
        if (vehiculo == null || vehiculo.getId() == null) {
            return null;
        }
        return userVehiculoRepository
                .findByFkUserVehiculo_IdAndFkVehiculoUser_Id(usuarioActual().getId(), vehiculo.getId())
                // Filas creadas antes del acceso compartido pueden venir sin rol: eran dueños.
                .map(vinculo -> vinculo.getRol() == null ? RolVehiculo.PROPIETARIO : vinculo.getRol())
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public void exigirVer(Vehiculo vehiculo) {
        if (rolDe(vehiculo) == null) {
            throw new AccesoDenegadoException("No tienes acceso a este vehiculo");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exigirEditar(Vehiculo vehiculo) {
        RolVehiculo rol = rolDe(vehiculo);
        if (!puedeEditar(rol)) {
            throw new AccesoDenegadoException(
                    rol == null
                            ? "No tienes acceso a este vehiculo"
                            : "Tu acceso a este vehiculo es de solo lectura");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exigirPropietario(Vehiculo vehiculo) {
        RolVehiculo rol = rolDe(vehiculo);
        if (rol != RolVehiculo.PROPIETARIO) {
            throw new AccesoDenegadoException(
                    rol == null
                            ? "No tienes acceso a este vehiculo"
                            : "Solo el propietario puede realizar esta accion");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVehiculo> vinculosDelUsuarioActual() {
        return userVehiculoRepository.findByFkUserVehiculo_Id(usuarioActual().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> vehiculosVisiblesIds() {
        return vinculosDelUsuarioActual().stream()
                .map(vinculo -> vinculo.getFkVehiculoUser().getId())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean puedeEditar(RolVehiculo rol) {
        return rol == RolVehiculo.PROPIETARIO || rol == RolVehiculo.COLABORADOR;
    }
}
