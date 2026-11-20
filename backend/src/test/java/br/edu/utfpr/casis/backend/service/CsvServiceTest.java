package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvServiceTest {

    private final CsvService csvService = new CsvService();

    @Test
    void deveExtrairAlunosDeCsvSeparadoPorPontoVirgula() {
        String csv = """
                Nome completo;RA;E-mail
                Ana Silva;12345;ana@alunos.utfpr.edu.br
                Bruno Souza;67890;bruno@alunos.utfpr.edu.br
                """;

        List<AlunoCertificado> alunos = csvService.extrairAlunos(criarArquivoCsv(csv));

        assertEquals(2, alunos.size());
        assertEquals("Ana Silva", alunos.getFirst().getNome());
        assertEquals("12345", alunos.getFirst().getRa());
        assertEquals("ana@alunos.utfpr.edu.br", alunos.getFirst().getEmail());
    }

    @Test
    void deveExtrairAlunosDeCsvSeparadoPorVirgula() {
        String csv = """
                Nome completo,RA,E-mail
                Carla Lima,54321,carla@alunos.utfpr.edu.br
                """;

        List<AlunoCertificado> alunos = csvService.extrairAlunos(criarArquivoCsv(csv));

        assertEquals(1, alunos.size());
        assertEquals("Carla Lima", alunos.getFirst().getNome());
        assertEquals("54321", alunos.getFirst().getRa());
        assertEquals("carla@alunos.utfpr.edu.br", alunos.getFirst().getEmail());
    }

    @Test
    void deveLerLinhasNumeradasConsiderandoCabecalhoComoLinhaUm() {
        String csv = """
                Nome completo;RA;E-mail
                Ana Silva;12345;ana@alunos.utfpr.edu.br
                Bruno Souza;67890;bruno@alunos.utfpr.edu.br
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals(List.of(
                new LinhaCsv(2, "Ana Silva", "ana@alunos.utfpr.edu.br", "12345"),
                new LinhaCsv(3, "Bruno Souza", "bruno@alunos.utfpr.edu.br", "67890")
        ), linhas);
    }

    @Test
    void deveIgnorarLinhasTotalmenteEmBrancoMantendoNumeracaoDoArquivo() {
        String csv = """
                Nome,RA,E-mail
                Ana Silva,12345,ana@alunos.utfpr.edu.br
                ,,

                 , ,\s
                Bruno Souza,67890,bruno@alunos.utfpr.edu.br
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals(2, linhas.size());
        assertEquals(2, linhas.get(0).linha());
        assertEquals(6, linhas.get(1).linha());
    }

    @Test
    void deveManterLinhasIncompletasComCamposVazios() {
        String csv = """
                Nome;RA;E-mail
                Carla Lima
                ;111;sem-nome@alunos.utfpr.edu.br
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals(List.of(
                new LinhaCsv(2, "Carla Lima", "", null),
                new LinhaCsv(3, "", "sem-nome@alunos.utfpr.edu.br", "111")
        ), linhas);
    }

    @Test
    void deveRetornarRaNuloQuandoNaoHaColunaRa() {
        String csv = """
                Nome,E-mail
                Ana Silva,ana@alunos.utfpr.edu.br
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals(List.of(new LinhaCsv(2, "Ana Silva", "ana@alunos.utfpr.edu.br", null)), linhas);
    }

    @Test
    void deveLancarDadosInvalidosQuandoCsvVazio() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> csvService.lerLinhas(criarArquivoCsv("")));

        assertEquals("O arquivo CSV está vazio ou sem cabeçalho.", erro.getMessage());
    }

    @Test
    void deveLancarDadosInvalidosQuandoCabecalhoNaoTemEmail() {
        String csv = """
                Nome;RA
                Ana Silva;12345
                """;

        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> csvService.lerLinhas(criarArquivoCsv(csv)));

        assertEquals("Cabeçalho inválido. Não foi possível identificar as colunas de 'Nome' e 'E-mail'.", erro.getMessage());
    }

    private MockMultipartFile criarArquivoCsv(String conteudo) {
        return new MockMultipartFile(
                "arquivoCsv",
                "participantes.csv",
                "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8)
        );
    }
}
