package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.UserVehiculoRequest;
import com.auto.app.easycar.dto.UserVehiculoResponse;
import com.auto.app.easycar.entitis.RolVehiculo;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.AccesoDenegadoException;
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
    private final AccesoVehiculoService acceso;

    public UserVehiculoServiceImpl(UserVehiculoRepository repository,
                                   UsuarioRepository usuarioRepository,
                                   VehiculoRepository vehiculoRepository,
                                   AccesoVehiculoService acceso) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.acceso = acceso;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVehiculoResponse> findAll() {
        // Solo las asociaciones del usuario autenticado (antes devolvia las de todos).
        return repository.findByFkUserVehiculo_Id(acceso.usuarioActual().getId()).stream()
                .map(UserVehiculoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserVehiculoResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVehiculoResponse> findByUsuario(UUID usuarioId) {
        if (!acceso.usuarioActual().getId().equals(usuarioId)) {
            throw new AccesoDenegadoException("Solo puedes consultar tus propios accesos");
        }
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
        // Dar acceso directo (sin codigo) sigue siendo potestad del propietario.
        acceso.exigirPropietario(vehiculoRepository.findById(request.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el vehiculo con id " + request.vehiculoId())));
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
        acceso.exigirPropietario(userVehiculo.getFkVehiculoUser());
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
        UserVehiculo userVehiculo = getEntity(id);
        boolean seQuitaASiMismo = acceso.usuarioActual().getId()
                .equals(userVehiculo.getFkUserVehiculo().getId());
        if (!seQuitaASiMismo) {
            acceso.exigirPropietario(userVehiculo.getFkVehiculoUser());
        }
        if (userVehiculo.getRol() == RolVehiculo.PROPIETARIO) {
            throw new IllegalArgumentException("El propietario no puede quitarse el acceso a su propio vehiculo");
        }
        repository.delete(userVehiculo);
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
                userVehiculo.getFkVehiculoUser().getId(),
                (userVehiculo.getRol() == null ? RolVehiculo.PROPIETARIO : userVehiculo.getRol()).name()
        );
    }
}
