package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.TipoMantenimientoResumen;
import com.auto.app.easycar.dto.VehiculoRequest;
import com.auto.app.easycar.dto.VehiculoResponse;
import com.auto.app.easycar.entitis.TipoMantenimiento;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.mapper.HistorialMapper;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository repository;

    public VehiculoServiceImpl(VehiculoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> findAll() {
        return repository.findAll().stream().map(VehiculoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse findById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse findByPlaca(String placa) {
        return toResponse(repository.findByPlacaIgnoreCase(placa)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el vehiculo con placa " + placa)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> findByMarca(String marca) {
        return repository.findByMarcaContainingIgnoreCase(marca).stream()
                .map(VehiculoServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public VehiculoResponse create(VehiculoRequest request) {
        String placa = request.placa().trim();
        if (repository.existsByPlacaIgnoreCase(placa)) {
            throw new DuplicateResourceException("Ya existe un vehiculo con la placa " + placa);
        }
        Vehiculo vehiculo = new Vehiculo();
        apply(vehiculo, request);
        return toResponse(repository.save(vehiculo));
    }

    @Override
    @Transactional
    public VehiculoResponse update(UUID id, VehiculoRequest request) {
        Vehiculo vehiculo = getEntity(id);
        String placa = request.placa().trim();
        repository.findByPlacaIgnoreCase(placa).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new DuplicateResourceException("Ya existe otro vehiculo con la placa " + placa);
            }
        });
        apply(vehiculo, request);
        return toResponse(repository.save(vehiculo));
    }

    @Override
    @Transactional
    public VehiculoResponse actualizarKilometraje(UUID id, Integer kilometrajeActual) {
        Vehiculo vehiculo = getEntity(id);
        vehiculo.setKilometrajeActual(kilometrajeActual);
        return toResponse(repository.save(vehiculo));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(getEntity(id));
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

    private static VehiculoResponse toResponse(Vehiculo vehiculo) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getNombre(),
                vehiculo.getMarca(),
                vehiculo.getFechaMatricula(),
                vehiculo.getKilometrajeActual(),
                toTiposResumen(vehiculo.getFkTipoMantenimiento()),
                HistorialMapper.toResumenes(vehiculo.getFkVehiculoHistorial())
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
                        tipo.getTipoVista()))
                .toList();
    }
}
