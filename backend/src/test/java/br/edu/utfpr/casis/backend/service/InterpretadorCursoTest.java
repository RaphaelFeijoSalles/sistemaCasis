package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.config.GestaoProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpretadorCursoTest {

    private static final GestaoProperties.Curso SI = new GestaoProperties.Curso(
            "Bacharelado em Sistemas de Informação", "Sistemas de Informação",
            List.of("SI", "Sistemas de Informação"));
    private static final GestaoProperties.Curso CDIA = new GestaoProperties.Curso(
            "Bacharelado em Ciência de Dados e Inteligência Artificial", "Ciência de Dados e Inteligência Artificial",
            List.of("CDIA", "Ciência de Dados e Inteligência Artificial"));

    private final InterpretadorCurso interpretador = new InterpretadorCurso(new GestaoProperties(
            "Diretório Acadêmico de Computação", "DACOMP", "Londrina", "x.png",
            Map.of("SI", SI, "CDIA", CDIA)));

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            "SI                                          | SI",
            "si                                          | SI",
            "' Sistemas de Informação '                  | SI",
            "sistemas de informacao                      | SI",
            "SISTEMAS  DE   INFORMAÇÃO                   | SI",
            "CDIA                                        | CDIA",
            "cdia                                        | CDIA",
            "Ciência de Dados e Inteligência Artificial  | CDIA",
            "ciencia de dados e inteligencia artificial  | CDIA",
    })
    void interpretaApelidosSemCaixaNemAcento(String valor, String codigoEsperado) {
        assertEquals(Optional.of(codigoEsperado), interpretador.interpretar(valor));
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {"ADS", "Sistemas", "CD", "S I", "Bacharelado em Sistemas de Informação"})
    void valoresForaDaListaSaoInvalidos(String valor) {
        assertTrue(interpretador.interpretar(valor).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void vazioNaoEhCurso(String valor) {
        assertTrue(interpretador.interpretar(valor).isEmpty());
    }

    @Test
    void ignoraBomNoInicioDoValor() {
        assertEquals(Optional.of("SI"), interpretador.interpretar("\uFEFFSI"));
    }

    @Test
    void devolveOCursoPeloCodigo() {
        assertEquals(Optional.of(SI), interpretador.curso("SI"));
        assertEquals(Optional.of(CDIA), interpretador.curso("cdia"));
        assertTrue(interpretador.curso("ADS").isEmpty());
        assertTrue(interpretador.curso(null).isEmpty());
    }
}
