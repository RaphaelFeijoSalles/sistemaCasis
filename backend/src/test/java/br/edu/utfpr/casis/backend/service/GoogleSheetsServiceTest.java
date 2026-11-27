package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testa só a formatação pura da coluna {@code data_fim} (F3: CA10), sem Spring e sem a API do Google.
 */
class GoogleSheetsServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 4, 1);

    @Test
    void fimAusenteDeixaAColunaVazia() {
        assertEquals("", GoogleSheetsService.formatarDataFim(INICIO, null));
    }

    @Test
    void fimIgualAoInicioEhEventoDeUmDiaEDeixaAColunaVazia() {
        assertEquals("", GoogleSheetsService.formatarDataFim(INICIO, INICIO));
    }

    @Test
    void fimDiferenteDoInicioSaiEmDiaMesAno() {
        assertEquals("05/04/2026", GoogleSheetsService.formatarDataFim(INICIO, LocalDate.of(2026, 4, 5)));
    }
}
