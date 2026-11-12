package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.AccesoVehiculoResponse;
import com.auto.app.easycar.dto.CambiarRolRequest;
import com.auto.app.easycar.dto.CanjeInvitacionResponse;
import com.auto.app.easycar.dto.InvitacionRequest;
import com.auto.app.easycar.dto.InvitacionResponse;
import com.auto.app.easycar.dto.RenovarInvitacionRequest;

import java.util.List;
import java.util.UUID;

public interface InvitacionVehiculoService {

    InvitacionResponse crear(InvitacionRequest request);

    List<InvitacionResponse> listarPorVehiculo(UUID vehiculoId);

    InvitacionResponse renovar(Long id, RenovarInvitacionRequest request);

    void revocar(Long id);

    CanjeInvitacionResponse canjear(String codigo);

    List<AccesoVehiculoResponse> listarAccesos(UUID vehiculoId);

    AccesoVehiculoResponse cambiarRol(UUID vehiculoId, UUID usuarioId, CambiarRolRequest request);

    void quitarAcceso(UUID vehiculoId, UUID usuarioId);
}
