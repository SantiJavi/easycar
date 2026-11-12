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
import java.util.Set;
import java.util.UUID;

@Service
public class TipoMantenimientoServiceImpl implements TipoMantenimientoService {

    /** tipoVista: 1 = por kilometraje, 2 = por fecha. */
    private static final int TIPO_VISTA_KILOMETRAJE = 1;
    private static final int TIPO_VISTA_FECHA = 2;

    private final TipoMantenimientoRepository repository;
    private final VehiculoRepository vehiculoRepository;
    private final AccesoVehiculoService acceso;

    public TipoMantenimientoServiceImpl(TipoMantenimientoRepository repository,
                                        VehiculoRepository vehiculoRepository,
                                        AccesoVehiculoService acceso) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
        this.acceso = acceso;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMantenimientoResponse> findAll() {
        Set<UUID> visibles = acceso.vehiculosVisiblesIds();
        return repository.findAll().stream()
                .filter(tipo -> visibles.contains(tipo.getFkTipoMantenimiento().getId()))
                .map(TipoMantenimientoServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoMantenimientoResponse findById(Long id) {
        TipoMantenimiento tipoMantenimiento = getEntity(id);
        acceso.exigirVer(tipoMantenimiento.getFkTipoMantenimiento());
        return toResponse(tipoMantenimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMantenimientoResponse> findByVehiculo(UUID vehiculoId) {
        acceso.exigirVer(getVehiculo(vehiculoId));
        return repository.findByFkTipoMantenimiento_Id(vehiculoId).stream()
                .map(TipoMantenimientoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public TipoMantenimientoResponse create(TipoMantenimientoRequest request) {
        Vehiculo vehiculo = getVehiculo(request.vehiculoId());
        acceso.exigirEditar(vehiculo);
        TipoMantenimiento tipoMantenimiento = new TipoMantenimiento();
        apply(tipoMantenimiento, request, vehiculo);
        return toResponse(repository.save(tipoMantenimiento));
    }

    @Override
    @Transactional
    public TipoMantenimientoResponse update(Long id, TipoMantenimientoRequest request) {
        TipoMantenimiento tipoMantenimiento = getEntity(id);
        acceso.exigirEditar(tipoMantenimiento.getFkTipoMantenimiento());
        Vehiculo vehiculo = getVehiculo(request.vehiculoId());
        // Si se mueve el mantenimiento a otro vehiculo, tambien hay que poder editarlo.
        acceso.exigirEditar(vehiculo);
        apply(tipoMantenimiento, request, vehiculo);
        return toResponse(repository.save(tipoMantenimiento));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TipoMantenimiento tipoMantenimiento = getEntity(id);
        acceso.exigirEditar(tipoMantenimiento.getFkTipoMantenimiento());
        repository.delete(tipoMantenimiento);
    }

    private TipoMantenimiento getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el tipo de mantenimiento con id " + id));
    }

    private Vehiculo getVehiculo(UUID vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el vehiculo con id " + vehiculoId));
    }

    private void apply(TipoMantenimiento tipoMantenimiento, TipoMantenimientoRequest request, Vehiculo vehiculo) {
        // El objetivo que no corresponde al tipo elegido se limpia: al pasar de
        // kilometraje a fecha (o al reves) no se arrastra la meta anterior.
        int tipoVista = request.tipoVista() == null ? TIPO_VISTA_KILOMETRAJE : request.tipoVista();
        boolean porFecha = tipoVista == TIPO_VISTA_FECHA;
        tipoMantenimiento.setNombreMantenimiento(request.nombreMantenimiento());
        tipoMantenimiento.setFechaSiguienteMantenimiento(
                porFecha ? request.fechaSiguienteMantenimiento() : null);
        tipoMantenimiento.setObservacion(request.observacion());
        tipoMantenimiento.setKilometrajeObjetivo(porFecha ? null : request.kilometrajeObjetivo());
        // La frecuencia es lo que permite recalcular el proximo objetivo y la barra de progreso.
        tipoMantenimiento.setFrecuenciaKm(porFecha ? null : request.frecuenciaKm());
        tipoMantenimiento.setFrecuenciaMeses(porFecha ? request.frecuenciaMeses() : null);
        tipoMantenimiento.setTipoVista(tipoVista);
        tipoMantenimiento.setFkTipoMantenimiento(vehiculo);
    }

    private static TipoMantenimientoResponse toResponse(TipoMantenimiento tipoMantenimiento) {
        return new TipoMantenimientoResponse(
                tipoMantenimiento.getId(),
                tipoMantenimiento.getNombreMantenimiento(),
                tipoMantenimiento.getFechaSiguienteMantenimiento(),
                tipoMantenimiento.getObservacion(),
                tipoMantenimiento.getKilometrajeObjetivo(),
                tipoMantenimiento.getFrecuenciaKm(),
                tipoMantenimiento.getFrecuenciaMeses(),
                tipoMantenimiento.getTipoVista(),
                tipoMantenimiento.getFkTipoMantenimiento().getId()
        );
    }
}
