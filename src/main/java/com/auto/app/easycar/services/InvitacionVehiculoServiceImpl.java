package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.AccesoVehiculoResponse;
import com.auto.app.easycar.dto.CambiarRolRequest;
import com.auto.app.easycar.dto.CanjeInvitacionResponse;
import com.auto.app.easycar.dto.InvitacionRequest;
import com.auto.app.easycar.dto.InvitacionResponse;
import com.auto.app.easycar.dto.RenovarInvitacionRequest;
import com.auto.app.easycar.entitis.InvitacionVehiculo;
import com.auto.app.easycar.entitis.RolVehiculo;
import com.auto.app.easycar.entitis.UserVehiculo;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.entitis.Vehiculo;
import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.InvitacionNoDisponibleException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.repositories.InvitacionVehiculoRepository;
import com.auto.app.easycar.repositories.UserVehiculoRepository;
import com.auto.app.easycar.repositories.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class InvitacionVehiculoServiceImpl implements InvitacionVehiculoService {

    private static final long MS_DIA = 86_400_000L;
    /** Sin caracteres ambiguos (sin I, L, O, U, 0 ni 1). */
    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTVWXYZ23456789";
    private static final int LARGO_CODIGO = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InvitacionVehiculoRepository invitacionRepository;
    private final UserVehiculoRepository userVehiculoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final AccesoVehiculoService acceso;

    public InvitacionVehiculoServiceImpl(InvitacionVehiculoRepository invitacionRepository,
                                         UserVehiculoRepository userVehiculoRepository,
                                         VehiculoRepository vehiculoRepository,
                                         AccesoVehiculoService acceso) {
        this.invitacionRepository = invitacionRepository;
        this.userVehiculoRepository = userVehiculoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.acceso = acceso;
    }

    @Override
    @Transactional
    public InvitacionResponse crear(InvitacionRequest request) {
        Vehiculo vehiculo = getVehiculo(request.vehiculoId());
        acceso.exigirPropietario(vehiculo);

        Date ahora = new Date();
        InvitacionVehiculo invitacion = new InvitacionVehiculo();
        invitacion.setCodigo(generarCodigo());
        invitacion.setFkVehiculoInvitacion(vehiculo);
        invitacion.setFkUsuarioInvitacion(acceso.usuarioActual());
        invitacion.setFechaCreacion(ahora);
        invitacion.setExpiraEn(sumarDias(ahora, request.diasValidez()));
        return toResponse(invitacionRepository.save(invitacion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitacionResponse> listarPorVehiculo(UUID vehiculoId) {
        Vehiculo vehiculo = getVehiculo(vehiculoId);
        acceso.exigirPropietario(vehiculo);
        return invitacionRepository.findByFkVehiculoInvitacion_IdOrderByFechaCreacionDesc(vehiculoId)
                .stream()
                .map(InvitacionVehiculoServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InvitacionResponse renovar(Long id, RenovarInvitacionRequest request) {
        InvitacionVehiculo invitacion = getInvitacion(id);
        acceso.exigirPropietario(invitacion.getFkVehiculoInvitacion());
        if (invitacion.isRevocada()) {
            throw new InvitacionNoDisponibleException(
                    "La invitacion fue revocada; genera un codigo nuevo para volver a compartir");
        }
        invitacion.setExpiraEn(sumarDias(new Date(), request.diasValidez()));
        return toResponse(invitacionRepository.save(invitacion));
    }

    @Override
    @Transactional
    public void revocar(Long id) {
        InvitacionVehiculo invitacion = getInvitacion(id);
        acceso.exigirPropietario(invitacion.getFkVehiculoInvitacion());
        invitacion.setRevocada(true);
        invitacionRepository.save(invitacion);
    }

    @Override
    @Transactional
    public CanjeInvitacionResponse canjear(String codigo) {
        String normalizado = codigo == null ? "" : codigo.trim().toUpperCase();
        InvitacionVehiculo invitacion = invitacionRepository.findByCodigoIgnoreCase(normalizado)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un codigo de acceso con ese valor"));
        if (invitacion.isRevocada()) {
            throw new InvitacionNoDisponibleException("El codigo fue revocado por el propietario");
        }
        if (invitacion.getExpiraEn() == null || invitacion.getExpiraEn().before(new Date())) {
            throw new InvitacionNoDisponibleException("El codigo esta vencido; pide uno nuevo");
        }

        Vehiculo vehiculo = invitacion.getFkVehiculoInvitacion();
        Usuario usuario = acceso.usuarioActual();
        var existente = userVehiculoRepository
                .findByFkUserVehiculo_IdAndFkVehiculoUser_Id(usuario.getId(), vehiculo.getId());
        if (existente.isPresent()) {
            boolean esPropietario = rolEfectivo(existente.get()) == RolVehiculo.PROPIETARIO;
            throw new DuplicateResourceException(esPropietario
                    ? "Ya eres el propietario de este vehiculo"
                    : "Ya tienes acceso a este vehiculo");
        }

        UserVehiculo vinculo = new UserVehiculo();
        vinculo.setFkUserVehiculo(usuario);
        vinculo.setFkVehiculoUser(vehiculo);
        vinculo.setRol(RolVehiculo.COLABORADOR);
        vinculo.setFechaAlta(new Date());
        userVehiculoRepository.save(vinculo);

        return new CanjeInvitacionResponse(
                vehiculo.getId(), vehiculo.getPlaca(), vehiculo.getNombre(), RolVehiculo.COLABORADOR.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccesoVehiculoResponse> listarAccesos(UUID vehiculoId) {
        Vehiculo vehiculo = getVehiculo(vehiculoId);
        acceso.exigirVer(vehiculo);
        return userVehiculoRepository.findByFkVehiculoUser_Id(vehiculoId).stream()
                .sorted(Comparator
                        .comparing((UserVehiculo vinculo) -> rolEfectivo(vinculo) != RolVehiculo.PROPIETARIO)
                        .thenComparing(vinculo -> vinculo.getFkUserVehiculo().getUsuario()))
                .map(InvitacionVehiculoServiceImpl::toAcceso)
                .toList();
    }

    @Override
    @Transactional
    public AccesoVehiculoResponse cambiarRol(UUID vehiculoId, UUID usuarioId, CambiarRolRequest request) {
        Vehiculo vehiculo = getVehiculo(vehiculoId);
        acceso.exigirPropietario(vehiculo);
        if (request.rol() == RolVehiculo.PROPIETARIO) {
            throw new IllegalArgumentException(
                    "Solo existe un propietario por vehiculo; asigna COLABORADOR o LECTURA");
        }
        UserVehiculo vinculo = getVinculo(vehiculoId, usuarioId);
        if (rolEfectivo(vinculo) == RolVehiculo.PROPIETARIO) {
            throw new IllegalArgumentException("No se puede cambiar el rol del propietario");
        }
        vinculo.setRol(request.rol());
        return toAcceso(userVehiculoRepository.save(vinculo));
    }

    @Override
    @Transactional
    public void quitarAcceso(UUID vehiculoId, UUID usuarioId) {
        Vehiculo vehiculo = getVehiculo(vehiculoId);
        UserVehiculo vinculo = getVinculo(vehiculoId, usuarioId);
        if (rolEfectivo(vinculo) == RolVehiculo.PROPIETARIO) {
            throw new IllegalArgumentException("El propietario no puede quitarse el acceso a su propio vehiculo");
        }
        // El propietario puede expulsar a cualquiera; un colaborador solo puede salirse a si mismo.
        boolean seQuitaASiMismo = acceso.usuarioActual().getId().equals(usuarioId);
        if (!seQuitaASiMismo) {
            acceso.exigirPropietario(vehiculo);
        }
        // El historial que haya registrado esta persona se conserva (no hay borrado en cascada).
        userVehiculoRepository.delete(vinculo);
    }

    private Vehiculo getVehiculo(UUID vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el vehiculo con id " + vehiculoId));
    }

    private InvitacionVehiculo getInvitacion(Long id) {
        return invitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la invitacion con id " + id));
    }

    private UserVehiculo getVinculo(UUID vehiculoId, UUID usuarioId) {
        return userVehiculoRepository
                .findByFkUserVehiculo_IdAndFkVehiculoUser_Id(usuarioId, vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario " + usuarioId + " no tiene acceso a este vehiculo"));
    }

    private static Date sumarDias(Date desde, Integer dias) {
        return new Date(desde.getTime() + dias * MS_DIA);
    }

    private String generarCodigo() {
        for (int intento = 0; intento < 10; intento++) {
            StringBuilder codigo = new StringBuilder(LARGO_CODIGO);
            for (int i = 0; i < LARGO_CODIGO; i++) {
                codigo.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
            }
            if (!invitacionRepository.existsByCodigo(codigo.toString())) {
                return codigo.toString();
            }
        }
        throw new IllegalStateException("No se pudo generar un codigo unico, intenta de nuevo");
    }

    private static InvitacionResponse toResponse(InvitacionVehiculo invitacion) {
        Date ahora = new Date();
        long msRestantes = invitacion.getExpiraEn().getTime() - ahora.getTime();
        long diasRestantes = msRestantes <= 0 ? 0 : (long) Math.ceil(msRestantes / (double) MS_DIA);
        return new InvitacionResponse(
                invitacion.getId(),
                invitacion.getCodigo(),
                invitacion.getFkVehiculoInvitacion().getId(),
                invitacion.getFechaCreacion(),
                invitacion.getExpiraEn(),
                estado(invitacion, ahora),
                diasRestantes);
    }

    private static String estado(InvitacionVehiculo invitacion, Date ahora) {
        if (invitacion.isRevocada()) {
            return "REVOCADA";
        }
        if (invitacion.getExpiraEn() == null || invitacion.getExpiraEn().before(ahora)) {
            return "EXPIRADA";
        }
        return "VIGENTE";
    }

    /** Las filas anteriores al acceso compartido no tienen rol: su dueño original es PROPIETARIO. */
    private static RolVehiculo rolEfectivo(UserVehiculo vinculo) {
        return vinculo.getRol() == null ? RolVehiculo.PROPIETARIO : vinculo.getRol();
    }

    private static AccesoVehiculoResponse toAcceso(UserVehiculo vinculo) {
        Usuario usuario = vinculo.getFkUserVehiculo();
        return new AccesoVehiculoResponse(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getEmail(),
                rolEfectivo(vinculo).name(),
                vinculo.getFechaAlta());
    }
}
