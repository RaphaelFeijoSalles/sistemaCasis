package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroAcademicoTest {

    @Test
    void deveTerMarcadorExternoEmMaiusculas() {
        assertEquals("EXTERNO", RegistroAcademico.EXTERNO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXTERNO", "externo", " Externo "})
    void deveReconhecerExternoQuandoIgualAoMarcadorSemCaixaESemBordas(String ra) {
        assertTrue(RegistroAcademico.ehExterno(ra));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "12345", "EXTERNOS", "EX TERNO"})
    void naoDeveReconhecerExternoQuandoVazioOuDiferenteDoMarcador(String ra) {
        assertFalse(RegistroAcademico.ehExterno(ra));
    }

    @Test
    void deveManterNuloQuandoNormalizarNulo() {
        assertNull(RegistroAcademico.normalizar(null));
    }

    @Test
    void deveRemoverEspacosDasBordasQuandoNormalizarRa() {
        assertEquals("12345", RegistroAcademico.normalizar(" 12345 "));
    }

    @Test
    void deveDevolverMarcadorCanonicoQuandoNormalizarExterno() {
        assertEquals("EXTERNO", RegistroAcademico.normalizar(" externo "));
    }

    @Test
    void deveDevolverVazioQuandoNormalizarVazio() {
        assertEquals("", RegistroAcademico.normalizar(""));
        assertEquals("", RegistroAcademico.normalizar("   "));
    }
}
