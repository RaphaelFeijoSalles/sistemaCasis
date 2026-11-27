package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testa só as partes puras da linha do Sheets, sem Spring e sem a API do Google:
 * a coluna {@code data_fim} (F3: CA10) e a montagem da linha (F8: CA7).
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

    // F8 (CA7): montagem da linha do Sheets

    private static final String CDIA = "Ciência de Dados e Inteligência Artificial";

    static Stream<Arguments> externosComQualquerCurso() {
        return Stream.of(
                Arguments.of("EXTERNO", null),
                Arguments.of(" externo ", ""),
                Arguments.of("Externo", "Sistemas de Informação"),
                Arguments.of("EXTERNO", "Sistemas de Informação"),
                Arguments.of(" externo ", null),
                Arguments.of("Externo", ""));
    }

    @ParameterizedTest
    @MethodSource("externosComQualquerCurso")
    void deveGravarExternoNormalizadoECursoVazioQuandoRaEhExterno(String ra, String curso) {
        List<Object> linha = GoogleSheetsService.montarLinha("Ana Silva", ra, curso, INICIO, "Semana Acadêmica", 1.5, null);

        assertEquals(List.of("Ana Silva", "EXTERNO", "", "01/04/2026", "Semana Acadêmica", "01:30:00", ""), linha);
    }

    @Test
    void deveManterRaECursoQuandoAlunoTemRa() {
        List<Object> linha = GoogleSheetsService.montarLinha("Ana Silva", "12345", CDIA, INICIO, "Semana Acadêmica", 2.0, null);

        assertEquals(List.of("Ana Silva", "12345", CDIA, "01/04/2026", "Semana Acadêmica", "02:00:00", ""), linha);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveGravarNaQuandoRaVazio(String ra) {
        List<Object> linha = GoogleSheetsService.montarLinha("Ana Silva", ra, CDIA, INICIO, "Semana Acadêmica", 2.0, null);

        assertEquals("N/A", linha.get(1));
        assertEquals(CDIA, linha.get(2));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveUsarSistemasDeInformacaoQuandoCursoVazioENaoExterno(String curso) {
        List<Object> linha = GoogleSheetsService.montarLinha("Ana Silva", "12345", curso, INICIO, "Semana Acadêmica", 2.0, null);

        assertEquals("12345", linha.get(1));
        assertEquals("Sistemas de Informação", linha.get(2));
    }

    @Test
    void deveTerSeteColunasComDataFimQuandoEventoLongo() {
        List<Object> linha = GoogleSheetsService.montarLinha("Ana Silva", "EXTERNO", null, INICIO, "Semana Acadêmica", 2.0,
                LocalDate.of(2026, 4, 5));

        assertEquals(7, linha.size());
        assertEquals("05/04/2026", linha.get(6));
    }

    @Test
    void deveDeixarDataFimVaziaQuandoFimIgualAoInicio() {
        List<Object> linha = GoogleSheetsService.montarLinha("Ana Silva", "12345", CDIA, INICIO, "Semana Acadêmica", 2.0, INICIO);

        assertEquals(7, linha.size());
        assertEquals("", linha.get(6));
    }
}
