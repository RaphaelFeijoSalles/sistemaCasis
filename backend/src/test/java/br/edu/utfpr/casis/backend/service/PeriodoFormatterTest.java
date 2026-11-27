package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeriodoFormatterTest {

    @Test
    void eventoDeUmDiaSemFimUsaEm() {
        assertEquals("em 15 de março de 2026",
                PeriodoFormatter.formatar(LocalDate.of(2026, 3, 15), null));
    }

    @Test
    void fimIgualAoInicioEhEventoDeUmDia() {
        assertEquals("em 15 de março de 2026",
                PeriodoFormatter.formatar(LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 15)));
    }

    @Test
    void mesmoMesCompactaMesEAno() {
        assertEquals("no período de 01 a 05 de abril de 2026",
                PeriodoFormatter.formatar(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5)));
    }

    @Test
    void mesesDiferentesNoMesmoAnoCompactaAno() {
        assertEquals("no período de 28 de abril a 02 de maio de 2026",
                PeriodoFormatter.formatar(LocalDate.of(2026, 4, 28), LocalDate.of(2026, 5, 2)));
    }

    @Test
    void anosDiferentesMostraDatasCompletas() {
        assertEquals("no período de 28 de dezembro de 2026 a 03 de janeiro de 2027",
                PeriodoFormatter.formatar(LocalDate.of(2026, 12, 28), LocalDate.of(2027, 1, 3)));
    }

    @Test
    void mesmoMesEmAnosDiferentesNaoCompacta() {
        assertEquals("no período de 10 de abril de 2026 a 05 de abril de 2027",
                PeriodoFormatter.formatar(LocalDate.of(2026, 4, 10), LocalDate.of(2027, 4, 5)));
    }
}
