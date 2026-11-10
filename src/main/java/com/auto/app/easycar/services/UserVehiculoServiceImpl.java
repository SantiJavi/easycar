package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.UserVehiculoRequest;
import com.auto.app.easycar.dto.UserVehiculoResponse;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.UserVehiculoRepository;
import com.auto.app.easycar.repositories.UsuarioRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserVehiculoServiceImpl implements UserVehiculoService {

    private final UserVehiculoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public UserVehiculoServiceImpl(UserVehiculoRepository repository,
                                   UsuarioRepository usuarioRepository,
                                   VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVehiculoResponse> findAll() {
        return repository.findAll().stream().map(UserVehiculoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserVehiculoResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVehiculoResponse> findByUsuario(UUID usuarioId) {
        return repository.findByFkUserVehiculo_Id(usuarioId).stream()
                .map(UserVehiculoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVehiculoResponse> findByVehiculo(UUID vehiculoId) {
        return repository.findByFkVehiculoUser_Id(vehiculoId).stream()
                .map(UserVehiculoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public UserVehiculoResponse create(UserVehiculoRequest request) {
        if (repository.existsByFkUserVehiculo_IdAndFkVehiculoUser_Id(request.usuarioId(), request.vehiculoId())) {
            throw new DuplicateResourceException(
                    "El usuario " + request.usuarioId() + " ya tiene asociado el vehiculo " + request.vehiculoId());
        }
        UserVehiculo userVehiculo = new UserVehiculo();
        apply(userVehiculo, request);
        return toResponse(repository.save(userVehiculo));
    }

    @Override
    @Transactional
    public UserVehiculoResponse update(Long id, UserVehiculoRequest request) {
        UserVehiculo userVehiculo = getEntity(id);
        if (repository.existsByFkUserVehiculo_IdAndFkVehiculoUser_Id(request.usuarioId(), request.vehiculoId())) {
            UserVehiculo actual = getEntity(id);
            boolean cambiaAsociacion = !actual.getFkUserVehiculo().getId().equals(request.usuarioId())
                    || !actual.getFkVehiculoUser().getId().equals(request.vehiculoId());
            if (cambiaAsociacion) {
                throw new DuplicateResourceException(
                        "El usuario " + request.usuarioId() + " ya tiene asociado el vehiculo " + request.vehiculoId());
            }
        }
        apply(userVehiculo, request);
        return toResponse(repository.save(userVehiculo));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
    }

    private UserVehiculo getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la asociacion usuario-vehiculo con id " + id));
    }

    private void apply(UserVehiculo userVehiculo, UserVehiculoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el usuario con id " + request.usuarioId()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el vehiculo con id " + request.vehiculoId()));
        userVehiculo.setFkUserVehiculo(usuario);
        userVehiculo.setFkVehiculoUser(vehiculo);
    }

    private static UserVehiculoResponse toResponse(UserVehiculo userVehiculo) {
        return new UserVehiculoResponse(
                userVehiculo.getId(),
                userVehiculo.getFkUserVehiculo().getId(),
                userVehiculo.getFkVehiculoUser().getId()
        );
    }
}
