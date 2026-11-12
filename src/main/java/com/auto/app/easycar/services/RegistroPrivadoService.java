package com.auto.app.easycar.services;

import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Puerta del alta de usuarios. Mientras no existan roles ni invitaciones, crear
 * cuentas requiere el codigo configurado en app.registro.codigo (variable de
 * entorno REGISTRO_CODIGO). Si no se configura, el registro queda cerrado.
 *
 * El valor se normaliza porque segun donde se defina la variable llega con
 * espacios al final, entre comillas literales o con otra capitalizacion.
 */
@Service
public class RegistroPrivadoService {

    private static final Logger log = LoggerFactory.getLogger(RegistroPrivadoService.class);

    private final String codigoConfigurado;

    public RegistroPrivadoService(@Value("${app.registro.codigo:}") String codigoConfigurado) {
        this.codigoConfigurado = normalizar(codigoConfigurado);
        if (habilitado()) {
            log.info("Registro privado habilitado: el codigo configurado tiene {} caracteres",
                    this.codigoConfigurado.length());
        } else {
            log.warn("Registro privado deshabilitado: defina app.registro.codigo o la variable REGISTRO_CODIGO");
        }
    }

    public boolean habilitado() {
        return !codigoConfigurado.isEmpty();
    }

    public void exigirCodigoValido(String codigoRecibido) {
        if (!habilitado()) {
            throw new AccesoDenegadoException(
                    "El registro de nuevos usuarios esta deshabilitado en este momento");
        }
        String candidato = normalizar(codigoRecibido);
        if (candidato.isEmpty() || !coincide(candidato)) {
            // Solo se registran longitudes: nunca el codigo.
            log.warn("Registro rechazado: el codigo enviado tiene {} caracteres y el configurado {}",
                    candidato.length(), codigoConfigurado.length());
            throw new AccesoDenegadoException("El codigo de registro no es valido");
        }
    }

    /** Comparacion en tiempo constante para no filtrar el codigo por tiempos de respuesta. */
    private boolean coincide(String candidato) {
        return MessageDigest.isEqual(
                codigoConfigurado.getBytes(StandardCharsets.UTF_8),
                candidato.getBytes(StandardCharsets.UTF_8));
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.trim();
        // Algunos entornos dejan las comillas como parte del valor ("codigo" o 'codigo').
        while (limpio.length() >= 2
                && ((limpio.startsWith("\"") && limpio.endsWith("\""))
                    || (limpio.startsWith("'") && limpio.endsWith("'")))) {
            limpio = limpio.substring(1, limpio.length() - 1).trim();
        }
        return limpio.toUpperCase(Locale.ROOT);
    }
}
