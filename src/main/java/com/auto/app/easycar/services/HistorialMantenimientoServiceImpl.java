package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.HistorialMantenimientoRequest;
import com.auto.app.easycar.dto.HistorialMantenimientoResponse;
import com.auto.app.easycar.entitis.HistorialMantenimientos;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.HistorialMantenimientosRepository;
import com.auto.app.easycar.repositories.TipoMantenimientoRepository;
import com.auto.app.easycar.repositories.UsuarioRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HistorialMantenimientoServiceImpl implements HistorialMantenimientoService {

    private final HistorialMantenimientosRepository repository;
    private final TipoMantenimientoRepository tipoMantenimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public HistorialMantenimientoServiceImpl(HistorialMantenimientosRepository repository,
                                             TipoMantenimientoRepository tipoMantenimientoRepository,
                                             UsuarioRepository usuarioRepository,
                                             VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.tipoMantenimientoRepository = tipoMantenimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findAll() {
        return repository.findAll().stream().map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialMantenimientoResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findByVehiculo(UUID vehiculoId) {
        return repository.findByFkVehiculoHistorial_Id(vehiculoId).stream()
                .map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findByUsuario(UUID usuarioId) {
        return repository.findByFkUserHistorial_Id(usuarioId).stream()
                .map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialMantenimientoResponse> findByTipoMantenimiento(Long tipoMantenimientoId) {
        return repository.findByFkTipoHistorial_Id(tipoMantenimientoId).stream()
                .map(HistorialMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public HistorialMantenimientoResponse create(HistorialMantenimientoRequest request) {
        HistorialMantenimientos historial = new HistorialMantenimientos();
        apply(historial, request);
        return toResponse(repository.save(historial));
    }

    @Override
    @Transactional
    public HistorialMantenimientoResponse update(Long id, HistorialMantenimientoRequest request) {
        HistorialMantenimientos historial = getEntity(id);
        apply(historial, request);
        return toResponse(repository.save(historial));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
    }

    private HistorialMantenimientos getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el registro de historial con id " + id));
    }

    private void apply(HistorialMantenimientos historial, HistorialMantenimientoRequest request) {
        TipoMantenimiento tipoMantenimiento = tipoMantenimientoRepository.findById(request.tipoMantenimientoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el tipo de mantenimiento con id " + request.tipoMantenimientoId()));
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el usuario con id " + request.usuarioId()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el vehiculo con id " + request.vehiculoId()));

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
