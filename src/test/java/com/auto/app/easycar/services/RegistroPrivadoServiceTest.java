package com.auto.app.easycar.services;

import com.auto.app.easycar.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistroPrivadoServiceTest {

    private static RegistroPrivadoService servicio(String codigoConfigurado) {
        return new RegistroPrivadoService(codigoConfigurado);
    }

    @Test
    void aceptaElCodigoExacto() {
        assertThatCode(() -> servicio("clave-secreta").exigirCodigoValido("clave-secreta"))
                .doesNotThrowAnyException();
    }

    @Test
    void ignoraEspaciosAlInicioYAlFinal() {
        // Un application.properties o un .env suele arrastrar espacios al final.
        assertThatCode(() -> servicio("clave-secreta  ").exigirCodigoValido("  clave-secreta "))
                .doesNotThrowAnyException();
    }

    @Test
    void aceptaDistintaCapitalizacion() {
        assertThatCode(() -> servicio("Clave-Secreta").exigirCodigoValido("clave-secreta"))
                .doesNotThrowAnyException();
    }

    @Test
    void aceptaElCodigoConfiguradoEntreComillas() {
        // Algunos gestores de variables de entorno dejan las comillas como parte del valor.
        assertThatCode(() -> servicio("\"clave-secreta\"").exigirCodigoValido("clave-secreta"))
                .doesNotThrowAnyException();
        assertThatCode(() -> servicio("'clave-secreta'").exigirCodigoValido("clave-secreta"))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaCodigoIncorrecto() {
        assertThatThrownBy(() -> servicio("clave-secreta").exigirCodigoValido("otra-cosa"))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("no es valido");
    }

    @Test
    void rechazaCodigoVacioONulo() {
        assertThatThrownBy(() -> servicio("clave-secreta").exigirCodigoValido(null))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThatThrownBy(() -> servicio("clave-secreta").exigirCodigoValido("   "))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void elRegistroQuedaDeshabilitadoSinCodigoConfigurado() {
        assertThatThrownBy(() -> servicio("").exigirCodigoValido("cualquiera"))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("deshabilitado");

        assertThatThrownBy(() -> servicio(null).exigirCodigoValido("cualquiera"))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("deshabilitado");
    }
}
