package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.VehiculoRequest;
import com.auto.app.easycar.dto.VehiculoResponse;

import java.util.List;
import java.util.UUID;

public interface VehiculoService {

    List<VehiculoResponse> findAll();

    VehiculoResponse findById(UUID id);

    VehiculoResponse findByPlaca(String placa);

    List<VehiculoResponse> findByMarca(String marca);

    VehiculoResponse create(VehiculoRequest request);

    VehiculoResponse update(UUID id, VehiculoRequest request);

    VehiculoResponse actualizarKilometraje(UUID id, Integer kilometrajeActual);

    void delete(UUID id);
}
