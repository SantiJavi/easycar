package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.TipoMantenimientoRequest;
import com.auto.app.easycar.dto.TipoMantenimientoResponse;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.TipoMantenimientoRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TipoMantenimientoServiceImpl implements TipoMantenimientoService {

    private final TipoMantenimientoRepository repository;
    private final VehiculoRepository vehiculoRepository;

    public TipoMantenimientoServiceImpl(TipoMantenimientoRepository repository,
                                        VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMantenimientoResponse> findAll() {
        return repository.findAll().stream().map(TipoMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoMantenimientoResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMantenimientoResponse> findByVehiculo(UUID vehiculoId) {
        return repository.findByFkTipoMantenimiento_Id(vehiculoId).stream()
                .map(TipoMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public TipoMantenimientoResponse create(TipoMantenimientoRequest request) {
        TipoMantenimiento tipoMantenimiento = new TipoMantenimiento();
        apply(tipoMantenimiento, request);
        return toResponse(repository.save(tipoMantenimiento));
    }

    @Override
    @Transactional
    public TipoMantenimientoResponse update(Long id, TipoMantenimientoRequest request) {
        TipoMantenimiento tipoMantenimiento = getEntity(id);
        apply(tipoMantenimiento, request);
        return toResponse(repository.save(tipoMantenimiento));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
    }

    private TipoMantenimiento getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el tipo de mantenimiento con id " + id));
    }

    private void apply(TipoMantenimiento tipoMantenimiento, TipoMantenimientoRequest request) {
        Vehiculo vehiculo = vehiculoRepository.findById(request.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el vehiculo con id " + request.vehiculoId()));
        tipoMantenimiento.setNombreMantenimiento(request.nombreMantenimiento());
        tipoMantenimiento.setFechaSiguienteMantenimiento(request.fechaSiguienteMantenimiento());
        tipoMantenimiento.setObservacion(request.observacion());
        tipoMantenimiento.setKilometrajeObjetivo(request.kilometrajeObjetivo());
        tipoMantenimiento.setTipoVista(request.tipoVista());
        tipoMantenimiento.setFkTipoMantenimiento(vehiculo);
    }

    private static TipoMantenimientoResponse toResponse(TipoMantenimiento tipoMantenimiento) {
        return new TipoMantenimientoResponse(
                tipoMantenimiento.getId(),
                tipoMantenimiento.getNombreMantenimiento(),
                tipoMantenimiento.getFechaSiguienteMantenimiento(),
                tipoMantenimiento.getObservacion(),
                tipoMantenimiento.getKilometrajeObjetivo(),
                tipoMantenimiento.getTipoVista(),
                tipoMantenimiento.getFkTipoMantenimiento().getId()
        );
    }
}
