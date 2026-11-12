package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.TipoMantenimientoResumen;
import com.auto.app.easycar.dto.VehiculoRequest;
import com.auto.app.easycar.dto.VehiculoResponse;
import com.auto.app.easycar.entitis.RolVehiculo;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.mapper.HistorialMapper;
import com.auto.app.easycar.repositories.InvitacionVehiculoRepository;
import com.auto.app.easycar.repositories.UserVehiculoRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository repository;
    private final UserVehiculoRepository userVehiculoRepository;
    private final InvitacionVehiculoRepository invitacionRepository;
    private final AccesoVehiculoService acceso;

    public VehiculoServiceImpl(VehiculoRepository repository,
                               UserVehiculoRepository userVehiculoRepository,
                               InvitacionVehiculoRepository invitacionRepository,
                               AccesoVehiculoService acceso) {
        this.repository = repository;
        this.userVehiculoRepository = userVehiculoRepository;
        this.invitacionRepository = invitacionRepository;
        this.acceso = acceso;
    }

    /** Solo los vehiculos a los que el usuario autenticado tiene acceso, con su rol. */
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> findAll() {
        return acceso.vinculosDelUsuarioActual().stream()
                .map(vinculo -> toResponse(vinculo.getFkVehiculoUser(), rolDe(vinculo)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse findById(UUID id) {
        Vehiculo vehiculo = getEntity(id);
        acceso.exigirVer(vehiculo);
        return toResponse(vehiculo, acceso.rolDe(vehiculo));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse findByPlaca(String placa) {
        Vehiculo vehiculo = repository.findByPlacaIgnoreCase(placa)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el vehiculo con placa " + placa));
        acceso.exigirVer(vehiculo);
        return toResponse(vehiculo, acceso.rolDe(vehiculo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> findByMarca(String marca) {
        Set<UUID> visibles = acceso.vehiculosVisiblesIds();
        return repository.findByMarcaContainingIgnoreCase(marca).stream()
                .filter(vehiculo -> visibles.contains(vehiculo.getId()))
                .map(vehiculo -> toResponse(vehiculo, acceso.rolDe(vehiculo)))
                .toList();
    }

    /**
     * Crea el vehiculo y deja al usuario autenticado como PROPIETARIO en la misma
     * transaccion, para que no existan vehiculos sin dueño.
     */
    @Override
    @Transactional
    public VehiculoResponse create(VehiculoRequest request) {
        String placa = request.placa().trim();
        if (repository.existsByPlacaIgnoreCase(placa)) {
            throw new DuplicateResourceException("Ya existe un vehiculo con la placa " + placa);
        }
        Vehiculo vehiculo = new Vehiculo();
        apply(vehiculo, request);
        Vehiculo guardado = repository.save(vehiculo);

        Usuario propietario = acceso.usuarioActual();
        UserVehiculo vinculo = new UserVehiculo();
        vinculo.setFkUserVehiculo(propietario);
        vinculo.setFkVehiculoUser(guardado);
        vinculo.setRol(RolVehiculo.PROPIETARIO);
        vinculo.setFechaAlta(new Date());
        userVehiculoRepository.save(vinculo);

        return toResponse(guardado, RolVehiculo.PROPIETARIO);
    }

    /** Datos del vehiculo (placa, marca, nombre): solo el propietario. */
    @Override
    @Transactional
    public VehiculoResponse update(UUID id, VehiculoRequest request) {
        Vehiculo vehiculo = getEntity(id);
        acceso.exigirPropietario(vehiculo);
        String placa = request.placa().trim();
        repository.findByPlacaIgnoreCase(placa).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new DuplicateResourceException("Ya existe otro vehiculo con la placa " + placa);
            }
        });
        apply(vehiculo, request);
        return toResponse(repository.save(vehiculo), RolVehiculo.PROPIETARIO);
    }

    @Override
    @Transactional
    public VehiculoResponse actualizarKilometraje(UUID id, Integer kilometrajeActual) {
        Vehiculo vehiculo = getEntity(id);
        acceso.exigirEditar(vehiculo);
        vehiculo.setKilometrajeActual(kilometrajeActual);
        return toResponse(repository.save(vehiculo), acceso.rolDe(vehiculo));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Vehiculo vehiculo = getEntity(id);
        acceso.exigirPropietario(vehiculo);
        // Los codigos y los accesos compartidos se van con el vehiculo;
        // el historial y los mantenimientos siguen bloqueando el borrado (409).
        invitacionRepository.deleteAll(
                invitacionRepository.findByFkVehiculoInvitacion_IdOrderByFechaCreacionDesc(id));
        userVehiculoRepository.deleteAll(userVehiculoRepository.findByFkVehiculoUser_Id(id));
        repository.delete(vehiculo);
    }

    private Vehiculo getEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el vehiculo con id " + id));
    }

    private static void apply(Vehiculo vehiculo, VehiculoRequest request) {
        vehiculo.setPlaca(request.placa().trim());
        vehiculo.setNombre(request.nombre());
        vehiculo.setMarca(request.marca());
        vehiculo.setFechaMatricula(request.fechaMatricula());
        vehiculo.setKilometrajeActual(request.kilometrajeActual());
    }

    private static RolVehiculo rolDe(UserVehiculo vinculo) {
        return vinculo.getRol() == null ? RolVehiculo.PROPIETARIO : vinculo.getRol();
    }

    private static VehiculoResponse toResponse(Vehiculo vehiculo, RolVehiculo rol) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getNombre(),
                vehiculo.getMarca(),
                vehiculo.getFechaMatricula(),
                vehiculo.getKilometrajeActual(),
                toTiposResumen(vehiculo.getFkTipoMantenimiento()),
                HistorialMapper.toResumenes(vehiculo.getFkVehiculoHistorial()),
                rol == null ? null : rol.name()
        );
    }

    /**
     * Vehiculo -> TipoMantenimiento (visible). El resumen no incluye el vehiculo de vuelta.
     */
    private static List<TipoMantenimientoResumen> toTiposResumen(List<TipoMantenimiento> tipos) {
        if (tipos == null) {
            return List.of();
        }
        return tipos.stream()
                .map(tipo -> new TipoMantenimientoResumen(
                        tipo.getId(),
                        tipo.getNombreMantenimiento(),
                        tipo.getFechaSiguienteMantenimiento(),
                        tipo.getObservacion(),
                        tipo.getKilometrajeObjetivo(),
                        tipo.getFrecuenciaKm(),
                        tipo.getFrecuenciaMeses(),
                        tipo.getTipoVista()))
                .toList();
    }
}
