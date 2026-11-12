package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.HistorialMantenimientoRequest;
import com.auto.app.easycar.dto.HistorialMantenimientoResponse;
import com.auto.app.easycar.entitis.HistorialMantenimientos;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.HistorialMantenimientosRepository;
import com.auto.app.easycar.repositories.TipoMantenimientoRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class HistorialMantenimientoServiceImpl implements HistorialMantenimientoService {

    private final HistorialMantenimientosRepository repository;
    private final TipoMantenimientoRepository tipoMantenimientoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final AccesoVehiculoService acceso;

    public HistorialMantenimientoServiceImpl(HistorialMantenimientosRepository repository,
                                             TipoMantenimientoRepository tipoMantenimientoRepository,
                                             VehiculoRepository vehiculoRepository,
                                             AccesoVehiculoService acceso) {
        this.repository = repository;
        this.tipoMantenimientoRepository = tipoMantenimientoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.acceso = acceso;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findAll() {
        Set<UUID> visibles = acceso.vehiculosVisiblesIds();
        if (visibles.isEmpty()) {
            return List.of();
        }
        return repository.findByFkVehiculoHistorial_IdIn(visibles).stream()
                .map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialMantenimientoResponse findById(Long id) {
        HistorialMantenimientos historial = getEntity(id);
        acceso.exigirVer(historial.getFkVehiculoHistorial());
        return toResponse(historial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findByVehiculo(UUID vehiculoId) {
        acceso.exigirVer(getVehiculo(vehiculoId));
        return repository.findByFkVehiculoHistorial_Id(vehiculoId).stream()
                .map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findByUsuario(UUID usuarioId) {
        // Cada usuario solo consulta su propio historial; los registros del vehiculo
        // compartido se ven desde el vehiculo.
        if (!acceso.usuarioActual().getId().equals(usuarioId)) {
            throw new AccesoDenegadoException("Solo puedes consultar tu propio historial");
        }
        return repository.findByFkUserHistorial_Id(usuarioId).stream()
                .map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findByTipoMantenimiento(Long tipoMantenimientoId) {
        TipoMantenimiento tipo = getTipo(tipoMantenimientoId);
        acceso.exigirVer(tipo.getFkTipoMantenimiento());
        return repository.findByFkTipoHistorial_Id(tipoMantenimientoId).stream()
                .map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public HistorialMantenimientoResponse create(HistorialMantenimientoRequest request) {
        Vehiculo vehiculo = getVehiculo(request.vehiculoId());
        acceso.exigirEditar(vehiculo);
        Usuario usuario = acceso.usuarioActual();
        if (!usuario.getId().equals(request.usuarioId())) {
            throw new AccesoDenegadoException(
                    "No puedes registrar un mantenimiento a nombre de otro usuario");
        }
        HistorialMantenimientos historial = new HistorialMantenimientos();
        apply(historial, request, vehiculo, usuario);
        return toResponse(repository.save(historial));
    }

    /** El registro conserva al usuario que lo hizo; cualquier persona con edicion puede corregirlo. */
    @Override
    @Transactional
    public HistorialMantenimientoResponse update(Long id, HistorialMantenimientoRequest request) {
        HistorialMantenimientos historial = getEntity(id);
        acceso.exigirEditar(historial.getFkVehiculoHistorial());
        Vehiculo vehiculo = getVehiculo(request.vehiculoId());
        acceso.exigirEditar(vehiculo);
        apply(historial, request, vehiculo, historial.getFkUserHistorial());
        return toResponse(repository.save(historial));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        HistorialMantenimientos historial = getEntity(id);
        // Cualquier persona con permiso de edicion puede borrar registros, aunque
        // los haya creado otra persona.
        acceso.exigirEditar(historial.getFkVehiculoHistorial());
        repository.delete(historial);
    }

    private HistorialMantenimientos getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el registro de historial con id " + id));
    }

    private Vehiculo getVehiculo(UUID vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el vehiculo con id " + vehiculoId));
    }

    private TipoMantenimiento getTipo(Long tipoMantenimientoId) {
        return tipoMantenimientoRepository.findById(tipoMantenimientoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el tipo de mantenimiento con id " + tipoMantenimientoId));
    }

    private void apply(HistorialMantenimientos historial, HistorialMantenimientoRequest request,
                       Vehiculo vehiculo, Usuario usuario) {
        TipoMantenimiento tipoMantenimiento = getTipo(request.tipoMantenimientoId());
        // El servicio pudo hacerse antes, nunca despues de hoy.
        if (request.fechaMantenimiento().after(new Date())) {
            throw new IllegalArgumentException("La fecha del mantenimiento no puede ser futura");
        }
        historial.setFechaMantenimiento(request.fechaMantenimiento());
        historial.setKilometrajeActual(request.kilometrajeActual());
        historial.setObservacion(request.observacion());
        historial.setFkTipoHistorial(tipoMantenimiento);
        historial.setFkUserHistorial(usuario);
        historial.setFkVehiculoHistorial(vehiculo);
    }

    private static HistorialMantenimientoResponse toResponse(HistorialMantenimientos historial) {
        return new HistorialMantenimientoResponse(
                historial.getId(),
                historial.getFechaMantenimiento(),
                historial.getKilometrajeActual(),
                historial.getObservacion(),
                historial.getFkTipoHistorial().getId(),
                historial.getFkUserHistorial().getId(),
                historial.getFkVehiculoHistorial().getId()
        );
    }
}
