package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.dto.PreVisualizacaoLoteDTO;
import br.edu.utfpr.casis.backend.dto.SituacaoLinha;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmissaoServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 20);

    @Mock
    private PdfService pdfService;
    @Mock
    private EmailService emailService;
    @Mock
    private GoogleDriveService driveService;
    @Mock
    private GoogleSheetsService sheetsService;

    private EmissaoService emissaoService;

    @BeforeEach
    void setUp() {
        emissaoService = new EmissaoService(new CsvService(), pdfService, emailService, driveService, sheetsService);
    }

    @AfterEach
    void nenhumEfeitoReal() {
        verifyNoInteractions(pdfService, emailService, driveService, sheetsService);
    }

    @Test
    void devePreVisualizarLoteComResumoESemEfeitos() {
        String csv = """
                Nome;RA;E-mail
                Ana Silva;1;ana@alunos.utfpr.edu.br
                Bruno Souza;;bruno@alunos.utfpr.edu.br
                ;;
                Carla Lima;3;carla@utfpr
                """;

        PreVisualizacaoLoteDTO previa = emissaoService.preVisualizarLote(lote(csv, 2.0));

        assertEquals(3, previa.totalLinhas());
        assertEquals(1, previa.validas());
        assertEquals(1, previa.comAviso());
        assertEquals(1, previa.comErro());
        assertEquals(List.of(2, 3, 5), previa.linhas().stream().map(l -> l.linha()).toList());
        assertEquals(List.of(SituacaoLinha.VALIDA, SituacaoLinha.AVISO, SituacaoLinha.ERRO),
                previa.linhas().stream().map(l -> l.situacao()).toList());
    }

    @Test
    void deveRecusarPreVisualizacaoComEventoInvalido() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(lote("Nome;E-mail\nAna;ana@x.com\n", 0.0)));

        assertEquals("A carga horária deve ser um número maior que zero.", erro.getMessage());
    }

    @Test
    void deveRecusarPreVisualizacaoSemArquivo() {
        EmissaoLoteEventoRequestDTO dto = new EmissaoLoteEventoRequestDTO("Semana Acadêmica", DATA, 2.0, null);

        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(dto));

        assertEquals("O arquivo CSV é obrigatório.", erro.getMessage());
    }

    @Test
    void deveRecusarPreVisualizacaoComCabecalhoInvalido() {
        assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(lote("Nome;RA\nAna;1\n", 2.0)));
    }

    private EmissaoLoteEventoRequestDTO lote(String csv, Double carga) {
        MockMultipartFile arquivo = new MockMultipartFile("arquivoCsv", "participantes.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));
        return new EmissaoLoteEventoRequestDTO("Semana Acadêmica", DATA, carga, arquivo);
    }
}
