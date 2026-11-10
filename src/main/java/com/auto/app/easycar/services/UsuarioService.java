package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.UsuarioRequest;
import com.auto.app.easycar.dto.UsuarioResponse;
import com.auto.app.easycar.dto.UsuarioUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UsuarioService {

    List<UsuarioResponse> findAll();

    UsuarioResponse findById(UUID id);

    UsuarioResponse findByEmail(String email);

    UsuarioResponse create(UsuarioRequest request);

    UsuarioResponse update(UUID id, UsuarioUpdateRequest request);

    void delete(UUID id);
}
