package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpretadorHorasTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            "2        | 2.0",
            "1.5      | 1.5",
            "1,5      | 1.5",
            "'  1,5 ' | 1.5",
            "01:30    | 1.5",
            "1:30:00  | 1.5",
            "1:30:45  | 1.5",
            "0:30     | 0.5",
            "0.25     | 0.25",
            "10:00    | 10.0",
            "100:15   | 100.25",
            "0:01     | 0.016666666666666666",
            "0,0084   | 0.0084",
            "1000     | 1000.0",
            "1000:00  | 1000.0"
    })
    void deveInterpretarValoresValidos(String texto, double esperado) {
        assertEquals(Optional.of(esperado), InterpretadorHoras.interpretar(texto));
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {"abc", "0", "0,0", "-2", "1:75", "0:00:45", "1.5h", "1:5", "1:30:75", ":30", "1.", ",5", "1.5.2", "1 30", "0:00"})
    void deveRecusarValoresInvalidos(String texto) {
        assertTrue(InterpretadorHoras.interpretar(texto).isEmpty());
    }

    @ParameterizedTest(name = "\"{0}\" fica abaixo de 1 minuto")
    @ValueSource(strings = {"0,001", "0.008", "0.0000001"})
    void deveRecusarValoresQueArredondamParaZeroMinutos(String texto) {
        assertTrue(InterpretadorHoras.interpretar(texto).isEmpty());
    }

    @ParameterizedTest(name = "\"{0}\" passa de 1.000 horas")
    @ValueSource(strings = {"1000,01", "1000:01", "1001", "99999999999", "999999999999999999999999999999"})
    void deveRecusarValoresAcimaDoMaximo(String texto) {
        assertTrue(InterpretadorHoras.interpretar(texto).isEmpty());
    }

    @Test
    void deveDefinirLimitesDeHoras() {
        assertTrue(InterpretadorHoras.dentroDosLimites(1.0 / 60));
        assertTrue(InterpretadorHoras.dentroDosLimites(1000.0));
        assertFalse(InterpretadorHoras.dentroDosLimites(0.008));
        assertFalse(InterpretadorHoras.dentroDosLimites(1000.01));
        assertFalse(InterpretadorHoras.dentroDosLimites(null));
        assertFalse(InterpretadorHoras.dentroDosLimites(Double.NaN));
        assertFalse(InterpretadorHoras.dentroDosLimites(Double.POSITIVE_INFINITY));
        assertFalse(InterpretadorHoras.dentroDosLimites(-1.0));
    }

    @Test
    void deveRecusarNuloEVazio() {
        assertTrue(InterpretadorHoras.interpretar(null).isEmpty());
        assertTrue(InterpretadorHoras.interpretar("").isEmpty());
        assertTrue(InterpretadorHoras.interpretar("   ").isEmpty());
    }
}
