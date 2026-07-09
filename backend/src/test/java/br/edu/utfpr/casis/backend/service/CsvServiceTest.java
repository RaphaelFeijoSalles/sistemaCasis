package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private MockMultipartFile criarArquivoCsv(String conteudo) {
        return new MockMultipartFile(
                "arquivoCsv",
                "participantes.csv",
                "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8)
        );
    }
}
