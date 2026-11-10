package com.auto.app.easycar.services;

import com.auto.app.easycar.dto.UsuarioRequest;
import com.auto.app.easycar.dto.UsuarioResponse;
import com.auto.app.easycar.dto.UsuarioUpdateRequest;
import com.auto.app.easycar.entitis.Usuario;
import com.auto.app.easycar.exceptions.DuplicateResourceException;
import com.auto.app.easycar.exceptions.ResourceNotFoundException;
import com.auto.app.easycar.mapper.HistorialMapper;
import com.auto.app.easycar.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return repository.findAll().stream().map(UsuarioServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse findById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse findByEmail(String email) {
        return toResponse(repository.findByEmailIgnoreCase(normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario con email " + email)));
    }

    @Override
    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        String email = normalize(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + email);
        }
        Usuario usuario = new Usuario();
        usuario.setUsuario(request.usuario().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(repository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse update(UUID id, UsuarioUpdateRequest request) {
        Usuario usuario = getEntity(id);
        String email = normalize(request.email());
        repository.findByEmailIgnoreCase(email).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new DuplicateResourceException("Ya existe otro usuario con el email " + email);
            }
        });
        usuario.setUsuario(request.usuario().trim());
        usuario.setEmail(email);
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(repository.save(usuario));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Usuario usuario = getEntity(id);
        repository.delete(usuario);
    }

    private Usuario getEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario con id " + id));
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static UsuarioResponse toResponse(Usuario usuario) {
        // Usuario -> historial (visible); el historial no devuelve el usuario.
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getEmail(),
                HistorialMapper.toResumenes(usuario.getFkUserHistorial()));
    }
}
