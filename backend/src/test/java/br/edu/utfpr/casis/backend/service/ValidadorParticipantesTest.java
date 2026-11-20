package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.LinhaValidadaDTO;
import br.edu.utfpr.casis.backend.dto.SituacaoLinha;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorParticipantesTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 20);

    @Test
    void deveMarcarLinhaCompletaComoValida() {
        List<LinhaValidadaDTO> resultado = ValidadorParticipantes.validarLinhas(List.of(
                new LinhaCsv(2, "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", null)
        ));

        assertEquals(List.of(new LinhaValidadaDTO(2, "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", SituacaoLinha.VALIDA, List.of())), resultado);
    }

    @Test
    void deveMarcarNomeVazioComoErro() {
        LinhaValidadaDTO linha = validarUma(new LinhaCsv(2, "", "ana@alunos.utfpr.edu.br", "12345", null));

        assertEquals(SituacaoLinha.ERRO, linha.situacao());
        assertEquals(List.of("Nome não informado"), linha.mensagens());
    }

    @Test
    void deveMarcarEmailVazioComoErro() {
        LinhaValidadaDTO linha = validarUma(new LinhaCsv(2, "Ana Silva", "", "12345", null));

        assertEquals(SituacaoLinha.ERRO, linha.situacao());
        assertEquals(List.of("E-mail não informado"), linha.mensagens());
    }

    @Test
    void deveMarcarEmailForaDoPadraoComoErro() {
        for (String email : List.of("ana", "ana@utfpr", "ana silva@utfpr.edu.br", "@utfpr.edu.br")) {
            LinhaValidadaDTO linha = validarUma(new LinhaCsv(2, "Ana Silva", email, "12345", null));

            assertEquals(SituacaoLinha.ERRO, linha.situacao(), email);
            assertEquals(List.of("E-mail inválido: " + email), linha.mensagens(), email);
        }
    }

    @Test
    void deveListarTodosOsErrosESemAvisoDeRaNaLinhaComErro() {
        LinhaValidadaDTO linha = validarUma(new LinhaCsv(2, "", "", null, null));

        assertEquals(SituacaoLinha.ERRO, linha.situacao());
        assertEquals(List.of("Nome não informado", "E-mail não informado"), linha.mensagens());
    }

    @Test
    void deveMarcarEmailRepetidoComoErroSemDiferenciarMaiusculas() {
        List<LinhaValidadaDTO> resultado = ValidadorParticipantes.validarLinhas(List.of(
                new LinhaCsv(2, "Ana Silva", "Ana@Alunos.utfpr.edu.br", "1", null),
                new LinhaCsv(3, "Ana S.", "ana@alunos.utfpr.edu.br", "1", null),
                new LinhaCsv(4, "Ana", "ANA@ALUNOS.UTFPR.EDU.BR", "1", null)
        ));

        assertEquals(SituacaoLinha.VALIDA, resultado.get(0).situacao());
        assertEquals(SituacaoLinha.ERRO, resultado.get(1).situacao());
        assertEquals(List.of("E-mail repetido (linha 2)"), resultado.get(1).mensagens());
        assertEquals(List.of("E-mail repetido (linha 2)"), resultado.get(2).mensagens());
    }

    @Test
    void naoDeveContarRepeticaoContraLinhaComErro() {
        List<LinhaValidadaDTO> resultado = ValidadorParticipantes.validarLinhas(List.of(
                new LinhaCsv(2, "", "ana@alunos.utfpr.edu.br", "1", null),
                new LinhaCsv(5, "Ana Silva", "ana@alunos.utfpr.edu.br", "1", null)
        ));

        assertEquals(SituacaoLinha.ERRO, resultado.get(0).situacao());
        assertEquals(SituacaoLinha.VALIDA, resultado.get(1).situacao());
    }

    @Test
    void deveAvisarHomonimoQueGeraMesmoNomeDePdf() {
        List<LinhaValidadaDTO> resultado = ValidadorParticipantes.validarLinhas(List.of(
                new LinhaCsv(2, "Ana Silva", "ana1@alunos.utfpr.edu.br", "1", null),
                new LinhaCsv(3, "Ana  Silva", "ana2@alunos.utfpr.edu.br", "2", null)
        ));

        assertEquals(SituacaoLinha.VALIDA, resultado.get(0).situacao());
        assertEquals(SituacaoLinha.AVISO, resultado.get(1).situacao());
        assertEquals(List.of("Mesmo nome da linha 2: o Drive terá dois arquivos com o mesmo nome"), resultado.get(1).mensagens());
    }

    @Test
    void naoDeveAvisarNomesQueDiferemEmMaiusculas() {
        List<LinhaValidadaDTO> resultado = ValidadorParticipantes.validarLinhas(List.of(
                new LinhaCsv(2, "Ana Silva", "ana1@alunos.utfpr.edu.br", "1", null),
                new LinhaCsv(3, "ANA SILVA", "ana2@alunos.utfpr.edu.br", "2", null)
        ));

        assertEquals(SituacaoLinha.VALIDA, resultado.get(1).situacao());
    }

    @Test
    void deveAvisarRaNuloOuVazio() {
        List<LinhaValidadaDTO> resultado = ValidadorParticipantes.validarLinhas(List.of(
                new LinhaCsv(2, "Ana Silva", "ana@alunos.utfpr.edu.br", null, null),
                new LinhaCsv(3, "Bruno Souza", "bruno@alunos.utfpr.edu.br", "", null)
        ));

        for (LinhaValidadaDTO linha : resultado) {
            assertEquals(SituacaoLinha.AVISO, linha.situacao());
            assertEquals(List.of("RA não informado"), linha.mensagens());
        }
    }

    @Test
    void deveAcumularAvisosDeHomonimoERa() {
        List<LinhaValidadaDTO> resultado = ValidadorParticipantes.validarLinhas(List.of(
                new LinhaCsv(2, "Ana Silva", "ana1@alunos.utfpr.edu.br", "1", null),
                new LinhaCsv(3, "Ana Silva", "ana2@alunos.utfpr.edu.br", null, null)
        ));

        assertEquals(List.of(
                "Mesmo nome da linha 2: o Drive terá dois arquivos com o mesmo nome",
                "RA não informado"
        ), resultado.get(1).mensagens());
    }

    @Test
    void deveAceitarEventoValido() {
        assertDoesNotThrow(() -> ValidadorParticipantes.validarEvento("Semana Acadêmica", DATA, 1.5));
    }

    @Test
    void deveRecusarNomeDoEventoNuloOuVazio() {
        for (String nome : new String[]{null, "", "   "}) {
            assertMensagem("O nome do evento é obrigatório.",
                    () -> ValidadorParticipantes.validarEvento(nome, DATA, 2.0));
        }
    }

    @Test
    void deveRecusarDataDeRealizacaoAusente() {
        assertMensagem("A data de realização é obrigatória.",
                () -> ValidadorParticipantes.validarEvento("Semana Acadêmica", null, 2.0));
    }

    @Test
    void deveRecusarCargaHorariaNulaZeroNegativaOuNaoNumerica() {
        for (Double carga : new Double[]{null, 0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertMensagem("A carga horária deve ser um número maior que zero.",
                    () -> ValidadorParticipantes.validarEvento("Semana Acadêmica", DATA, carga));
        }
    }

    @Test
    void deveAceitarParticipanteValido() {
        assertDoesNotThrow(() -> ValidadorParticipantes.validarParticipante("Ana Silva", " ana@alunos.utfpr.edu.br "));
    }

    @Test
    void deveRecusarNomeDoParticipanteNuloOuVazio() {
        for (String nome : new String[]{null, "", "  "}) {
            assertMensagem("O nome do participante é obrigatório.",
                    () -> ValidadorParticipantes.validarParticipante(nome, "ana@alunos.utfpr.edu.br"));
        }
    }

    @Test
    void deveRecusarEmailDoParticipanteAusente() {
        for (String email : new String[]{null, "", "  "}) {
            assertMensagem("O e-mail do participante é obrigatório.",
                    () -> ValidadorParticipantes.validarParticipante("Ana Silva", email));
        }
    }

    @Test
    void deveRecusarEmailDoParticipanteForaDoPadrao() {
        assertMensagem("E-mail do participante inválido: ana@utfpr",
                () -> ValidadorParticipantes.validarParticipante("Ana Silva", "ana@utfpr"));
    }

    private void assertMensagem(String esperada, Executable acao) {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class, acao);
        assertEquals(esperada, erro.getMessage());
    }

    private LinhaValidadaDTO validarUma(LinhaCsv linha) {
        return ValidadorParticipantes.validarLinhas(List.of(linha)).getFirst();
    }
}
