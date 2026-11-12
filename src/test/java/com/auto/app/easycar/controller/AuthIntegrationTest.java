package com.auto.app.easycar.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registroYLoginEntreganUnTokenValido() throws Exception {
        String email = registrar(emailUnico());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void elTokenPermiteAccederAUnRecursoProtegido() throws Exception {
        String token = tokenNuevoUsuario();

        mockMvc.perform(get("/api/vehiculos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void sinTokenDevuelve401ConMensajeJson() throws Exception {
        mockMvc.perform(get("/api/vehiculos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message", containsString("/auth/login")));
    }

    @Test
    void conTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/vehiculos").header(HttpHeaders.AUTHORIZATION, "Bearer token.falso.invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        String email = registrar(emailUnico());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"incorrecta"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void registroDuplicadoDevuelve409() throws Exception {
        String email = registrar(emailUnico());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"otro","email":"%s","password":"%s","codigo":"%s"}
                                """.formatted(email, PASSWORD, CODIGO_REGISTRO)))
                .andExpect(status().isConflict());
    }

    @Test
    void registroConDatosInvalidosDevuelve400ConErroresPorCampo() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"","email":"no-es-email","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.usuario").exists());
    }

    @Test
    void registroSinCodigoDevuelve400ConErrorEnElCampo() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"otro","email":"%s","password":"%s"}
                                """.formatted(emailUnico(), PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.codigo").exists());
    }

    @Test
    void registroConCodigoIncorrectoDevuelve403() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"otro","email":"%s","password":"%s","codigo":"otro-codigo"}
                                """.formatted(emailUnico(), PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("no es valido")));
    }

    @Test
    void crearUsuarioPorApiTambienExigeElCodigo() throws Exception {
        String token = tokenNuevoUsuario();

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"otro","email":"%s","password":"%s"}
                                """.formatted(emailUnico(), PASSWORD)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-Codigo-Registro", CODIGO_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"otro","email":"%s","password":"%s"}
                                """.formatted(emailUnico(), PASSWORD)))
                .andExpect(status().isCreated());
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laRespuestaDeUsuarioNoExponeLaContrasena() throws Exception {
        Sesion sesion = sesion();

        String body = mockMvc.perform(get("/api/usuarios/" + sesion.usuarioId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + sesion.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(sesion.email()))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(body)
                .doesNotContain("password")
                .doesNotContain("$2a$")
                .doesNotContain("$2b$");
    }

    @Test
    void elApiDocsExponeElEsquemaBearer() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.info.title").value("EasyCar API"));
    }

    @Test
    void elLoginEstaDocumentadoSinSeguridad() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // @SecurityRequirements sobre AuthController deja la operacion sin requerimiento de token
        Object seguridadLogin = JsonPath.read(body, "$['paths']['/auth/login']['post']['security']");
        org.assertj.core.api.Assertions.assertThat(seguridadLogin.toString()).isEqualTo("[]");

        Object seguridadVehiculos = JsonPath.read(body, "$['paths']['/api/vehiculos']['get']['security']");
        org.assertj.core.api.Assertions.assertThat(seguridadVehiculos.toString()).contains("bearerAuth");
    }
}
