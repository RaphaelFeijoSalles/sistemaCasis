package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
                new LinhaCsv(2, "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", null),
                new LinhaCsv(3, "Bruno Souza", "bruno@alunos.utfpr.edu.br", "67890", null)
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
                new LinhaCsv(2, "Carla Lima", "", null, null),
                new LinhaCsv(3, "", "sem-nome@alunos.utfpr.edu.br", "111", null)
        ), linhas);
    }

    @Test
    void deveRetornarRaNuloQuandoNaoHaColunaRa() {
        String csv = """
                Nome,E-mail
                Ana Silva,ana@alunos.utfpr.edu.br
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals(List.of(new LinhaCsv(2, "Ana Silva", "ana@alunos.utfpr.edu.br", null, null)), linhas);
    }

    @Test
    void deveLerColunaHorasComTextoBruto() {
        String csv = """
                Nome;E-mail;RA;Horas
                Ana Silva;ana@alunos.utfpr.edu.br;12345; 1,5\s
                Bruno Souza;bruno@alunos.utfpr.edu.br;67890;01:30
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals(List.of(
                new LinhaCsv(2, "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", "1,5"),
                new LinhaCsv(3, "Bruno Souza", "bruno@alunos.utfpr.edu.br", "67890", "01:30")
        ), linhas);
    }

    @Test
    void deveReconhecerCabecalhoHorasSemDiferenciarMaiusculasEEspacos() {
        String csv = """
                Nome,E-mail,  HORAS\s
                Ana Silva,ana@alunos.utfpr.edu.br,"1,5"
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals("1,5", linhas.getFirst().horas());
    }

    @Test
    void deveRetornarHorasNulaQuandoNaoHaColunaHoras() {
        String csv = """
                Nome;E-mail
                Ana Silva;ana@alunos.utfpr.edu.br
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertNull(linhas.getFirst().horas());
    }

    @Test
    void deveRetornarHorasVaziaOuNulaConformeCelula() {
        String csv = """
                Nome;E-mail;Horas
                Ana Silva;ana@alunos.utfpr.edu.br;
                Bruno Souza;bruno@alunos.utfpr.edu.br
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertEquals("", linhas.get(0).horas());
        assertNull(linhas.get(1).horas());
    }

    @Test
    void naoDeveReconhecerOutrosNomesDeColunaDeHoras() {
        String csv = """
                Nome;E-mail;Carga horária;CH;Hora
                Ana Silva;ana@alunos.utfpr.edu.br;2;2;2
                """;

        List<LinhaCsv> linhas = csvService.lerLinhas(criarArquivoCsv(csv));

        assertNull(linhas.getFirst().horas());
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
