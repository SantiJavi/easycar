package com.auto.app.easycar.controller;

import com.auto.app.easycar.dto.UsuarioRequest;
import com.auto.app.easycar.dto.UsuarioResponse;
import com.auto.app.easycar.dto.auth.LoginRequest;
import com.auto.app.easycar.dto.auth.LoginResponse;
import com.auto.app.easycar.dto.auth.RegistroRequest;
import com.auto.app.easycar.exceptions.ApiError;
import com.auto.app.easycar.services.AuthService;
import com.auto.app.easycar.services.RegistroPrivadoService;
import com.auto.app.easycar.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "Registro de usuarios e inicio de sesion para obtener el token JWT")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final RegistroPrivadoService registroPrivadoService;

    public AuthController(AuthService authService, UsuarioService usuarioService,
                          RegistroPrivadoService registroPrivadoService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.registroPrivadoService = registroPrivadoService;
    }

    @Operation(summary = "Iniciar sesion",
            description = "Valida email y contrasena y devuelve un token JWT para consumir el resto de la API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales validas, token emitido"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registrar usuario (restringido)",
            description = "Crea un usuario con la contrasena cifrada con BCrypt. Requiere el codigo "
                    + "de registro configurado en app.registro.codigo (REGISTRO_CODIGO). "
                    + "Si el servidor no tiene codigo, el registro esta deshabilitado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Codigo de registro ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "El email ya esta registrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegistroRequest request) {
        registroPrivadoService.exigirCodigoValido(request.codigo());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(
                new UsuarioRequest(request.usuario(), request.email(), request.password())));
    }
}
