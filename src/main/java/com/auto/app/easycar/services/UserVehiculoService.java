package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.UserVehiculoRequest;
import com.auto.app.easycar.dto.UserVehiculoResponse;

import java.util.List;
import java.util.UUID;

public interface UserVehiculoService {

    List<UserVehiculoResponse> findAll();

    UserVehiculoResponse findById(Long id);

    List<UserVehiculoResponse> findByUsuario(UUID usuarioId);

    List<UserVehiculoResponse> findByVehiculo(UUID vehiculoId);

    UserVehiculoResponse create(UserVehiculoRequest request);

    UserVehiculoResponse update(Long id, UserVehiculoRequest request);

    void delete(Long id);
}
