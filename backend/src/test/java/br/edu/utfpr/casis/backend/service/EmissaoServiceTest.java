package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoIndividualRequestDTO;
import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.dto.PreVisualizacaoLoteDTO;
import br.edu.utfpr.casis.backend.dto.ResultadoEmissaoDTO;
import br.edu.utfpr.casis.backend.dto.SituacaoLinha;
import br.edu.utfpr.casis.backend.dto.StatusEmissao;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmissaoServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 20);
    private static final String EVENTO = "Semana Acadêmica";
    private static final String PASTA = "pasta-teste";
    private static final byte[] PDF = {1, 2, 3};
    private static final String SI_CURTO = "Sistemas de Informação";
    private static final String CDIA_CURTO = "Ciência de Dados e Inteligência Artificial";

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
        emissaoService = new EmissaoService(new CsvService(), pdfService, emailService, driveService, sheetsService,
                GestaoDeTeste.interpretador());
    }

    // ---------- Pré-visualização (CA1) ----------

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
        semEfeitos();
    }

    @Test
    void deveRecusarPreVisualizacaoComEventoInvalido() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(lote("Nome;E-mail\nAna;ana@x.com\n", 0.0)));

        assertEquals("A carga horária deve ser um número maior que zero.", erro.getMessage());
        semEfeitos();
    }

    @Test
    void deveRecusarPreVisualizacaoSemArquivo() {
        EmissaoLoteEventoRequestDTO dto = new EmissaoLoteEventoRequestDTO(EVENTO, DATA, null, 2.0, null, null);

        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(dto));

        assertEquals("O arquivo CSV é obrigatório.", erro.getMessage());
        semEfeitos();
    }

    // ---------- Curso padrão (F4: CA8, CA9) ----------

    @Test
    void devePreVisualizarComCursoDaLinhaECursoPadraoDoFormulario() {
        String csv = """
                Nome;E-mail;Curso
                Ana Silva;ana@alunos.utfpr.edu.br;si
                Bruno Souza;bruno@alunos.utfpr.edu.br;
                Carla Lima;carla@alunos.utfpr.edu.br;ADS
                """;

        PreVisualizacaoLoteDTO previa = emissaoService.preVisualizarLote(lote(csv, 2.0, "CDIA"));

        assertEquals(Arrays.asList("SI", "CDIA", null), previa.linhas().stream().map(l -> l.curso()).toList());
        assertEquals(Arrays.asList(GestaoDeTeste.NOME_SI, GestaoDeTeste.NOME_CDIA, null),
                previa.linhas().stream().map(l -> l.cursoTexto()).toList());
        assertEquals(List.of(false, true, false), previa.linhas().stream().map(l -> l.cursoDoFormulario()).toList());
        assertEquals(List.of("Curso inválido: ADS"), previa.linhas().get(2).mensagens());
        semEfeitos();
    }

    @Test
    void deveUsarSiQuandoCursoPadraoAusente() {
        for (String ausente : new String[]{null, "", "  "}) {
            PreVisualizacaoLoteDTO previa = emissaoService.preVisualizarLote(
                    lote("Nome;E-mail\nAna;ana@alunos.utfpr.edu.br\n", 2.0, ausente));

            assertEquals("SI", previa.linhas().getFirst().curso());
            assertTrue(previa.linhas().getFirst().cursoDoFormulario());
        }
        semEfeitos();
    }

    @Test
    void deveAceitarApelidoNoCursoPadrao() {
        PreVisualizacaoLoteDTO previa = emissaoService.preVisualizarLote(
                lote("Nome;E-mail\nAna;ana@alunos.utfpr.edu.br\n", 2.0, "ciencia de dados e inteligencia artificial"));

        assertEquals("CDIA", previa.linhas().getFirst().curso());
    }

    @Test
    void deveRecusarCursoPadraoInvalidoNaPreVisualizacaoENoLoteAntesDeQualquerEfeito() {
        DadosInvalidosException erroPrevia = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(lote("Nome;E-mail\nAna;ana@x.com\n", 2.0, "ADS")));
        DadosInvalidosException erroLote = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissao(lote("Nome;E-mail\nAna;ana@x.com\n", 2.0, "ADS")));

        assertEquals("Curso inválido: ADS", erroPrevia.getMessage());
        assertEquals("Curso inválido: ADS", erroLote.getMessage());
        semEfeitos();
    }

    @Test
    void deveRecusarCursoDoUnitarioInvalidoAntesDeQualquerEfeito() {
        EmissaoIndividualRequestDTO dto = new EmissaoIndividualRequestDTO(EVENTO, DATA, null, 2.0,
                "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", "ADS");

        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissaoIndividual(dto));

        assertEquals("Curso inválido: ADS", erro.getMessage());
        semEfeitos();
    }

    @Test
    void deveRecusarPreVisualizacaoComCabecalhoInvalido() {
        assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(lote("Nome;RA\nAna;1\n", 2.0)));
        semEfeitos();
    }

    // ---------- Emissão em lote (CA6, CA7, CA8, CA11, CA4) ----------

    @Test
    void deveEmitirValidasEAvisosEPularLinhasComErro() {
        prepararDrive(Set.of());
        String csv = """
                Nome;RA;E-mail
                Ana Silva;1;ana@alunos.utfpr.edu.br
                Carla Lima;3;carla@utfpr
                ;;
                Bruno Souza;;bruno@alunos.utfpr.edu.br
                """;

        List<ResultadoEmissaoDTO> relatorio = emissaoService.processarEmissao(lote(csv, 2.0));

        assertEquals(List.of(
                new ResultadoEmissaoDTO("Ana Silva", "ana@alunos.utfpr.edu.br", StatusEmissao.SUCESSO, null),
                new ResultadoEmissaoDTO("Carla Lima", "carla@utfpr", StatusEmissao.ERRO, "E-mail inválido: carla@utfpr"),
                new ResultadoEmissaoDTO("Bruno Souza", "bruno@alunos.utfpr.edu.br", StatusEmissao.SUCESSO, null)
        ), relatorio);
        verify(driveService, times(1)).obterOuCriarPastaEvento(EVENTO, DATA);
        verify(driveService, times(1)).listarArquivosNaPasta(PASTA);
        verify(pdfService, times(2)).gerarCertificado(any(), any());
        verify(emailService, never()).enviarCertificado(eq("carla@utfpr"), anyString(), anyString(), any());
        verify(driveService).fazerUploadCertificado(PDF, "Certificado_Ana_Silva.pdf", PASTA);
        verify(driveService).fazerUploadCertificado(PDF, "Certificado_Bruno_Souza.pdf", PASTA);
        verify(sheetsService).registrarEmissao("Ana Silva", "1", SI_CURTO, DATA, EVENTO, 2.0, null);
        verify(sheetsService).registrarEmissao("Bruno Souza", "", SI_CURTO, DATA, EVENTO, 2.0, null);
    }

    @Test
    void deveManterOrdemDosEfeitosPorParticipante() {
        prepararDrive(Set.of());

        emissaoService.processarEmissao(lote("Nome;RA;E-mail\nAna Silva;1;ana@alunos.utfpr.edu.br\n", 2.0));

        InOrder ordem = inOrder(driveService, pdfService, emailService, sheetsService);
        ordem.verify(driveService).listarArquivosNaPasta(PASTA);
        ordem.verify(pdfService).gerarCertificado(any(), any());
        ordem.verify(emailService).enviarCertificado(eq("ana@alunos.utfpr.edu.br"), eq("Ana Silva"), eq(EVENTO), eq(PDF));
        ordem.verify(driveService).fazerUploadCertificado(PDF, "Certificado_Ana_Silva.pdf", PASTA);
        ordem.verify(sheetsService).registrarEmissao("Ana Silva", "1", SI_CURTO, DATA, EVENTO, 2.0, null);
    }

    @Test
    void deveManterAntiDuplicacaoPeloNomeDoArquivo() {
        when(driveService.obterOuCriarPastaEvento(EVENTO, DATA)).thenReturn(PASTA);
        when(driveService.listarArquivosNaPasta(PASTA)).thenReturn(Set.of("Certificado_Ana_Silva.pdf"));

        List<ResultadoEmissaoDTO> relatorio = emissaoService.processarEmissao(
                lote("Nome;RA;E-mail\n  Ana   Silva ;1;ana@alunos.utfpr.edu.br\n", 2.0));

        assertEquals(StatusEmissao.EXISTENTE, relatorio.getFirst().status());
        verifyNoInteractions(pdfService, emailService, sheetsService);
        verify(driveService, never()).fazerUploadCertificado(any(), anyString(), anyString());
    }

    @Test
    void deveEmitirParaOsDoisHomonimos() {
        prepararDrive(Set.of());
        String csv = """
                Nome;RA;E-mail
                Ana Silva;1;ana1@alunos.utfpr.edu.br
                Ana Silva;2;ana2@alunos.utfpr.edu.br
                """;

        List<ResultadoEmissaoDTO> relatorio = emissaoService.processarEmissao(lote(csv, 2.0));

        assertEquals(List.of(StatusEmissao.SUCESSO, StatusEmissao.SUCESSO), relatorio.stream().map(ResultadoEmissaoDTO::status).toList());
        verify(emailService).enviarCertificado(eq("ana1@alunos.utfpr.edu.br"), anyString(), anyString(), any());
        verify(emailService).enviarCertificado(eq("ana2@alunos.utfpr.edu.br"), anyString(), anyString(), any());
        verify(driveService, times(2)).fazerUploadCertificado(PDF, "Certificado_Ana_Silva.pdf", PASTA);
    }

    // ---------- Horas por participante (F2: CA6, CA7) ----------

    @Test
    void deveGerarPdfERegistrarPlanilhaComAsHorasDeCadaParticipante() {
        prepararDrive(Set.of());
        String csv = """
                Nome;RA;E-mail;Horas
                Ana Silva;1;ana@alunos.utfpr.edu.br;1,5
                Bruno Souza;2;bruno@alunos.utfpr.edu.br;
                Carla Lima;3;carla@alunos.utfpr.edu.br;01:00
                """;

        emissaoService.processarEmissao(lote(csv, 2.0));

        ArgumentCaptor<AlunoCertificado> alunos = ArgumentCaptor.forClass(AlunoCertificado.class);
        ArgumentCaptor<EmissaoLoteEventoRequestDTO> dadosPdf = ArgumentCaptor.forClass(EmissaoLoteEventoRequestDTO.class);
        verify(pdfService, times(3)).gerarCertificado(alunos.capture(), dadosPdf.capture());

        assertEquals(List.of("Ana Silva", "Bruno Souza", "Carla Lima"), alunos.getAllValues().stream().map(AlunoCertificado::getNome).toList());
        assertEquals(List.of(1.5, 2.0, 1.0), dadosPdf.getAllValues().stream().map(EmissaoLoteEventoRequestDTO::cargaHoraria).toList());
        for (EmissaoLoteEventoRequestDTO dados : dadosPdf.getAllValues()) {
            assertEquals(EVENTO, dados.nomeEvento());
            assertEquals(DATA, dados.dataRealizacao());
        }

        verify(sheetsService).registrarEmissao("Ana Silva", "1", SI_CURTO, DATA, EVENTO, 1.5, null);
        verify(sheetsService).registrarEmissao("Bruno Souza", "2", SI_CURTO, DATA, EVENTO, 2.0, null);
        verify(sheetsService).registrarEmissao("Carla Lima", "3", SI_CURTO, DATA, EVENTO, 1.0, null);
        verify(emailService, times(3)).enviarCertificado(anyString(), anyString(), eq(EVENTO), eq(PDF));
    }

    // ---------- Curso por participante (F4: CA10, CA11, CA14) ----------

    @Test
    void deveGerarPdfERegistrarPlanilhaComOCursoDeCadaParticipante() {
        prepararDrive(Set.of());
        String csv = """
                Nome;RA;E-mail;Curso
                Ana Silva;1;ana@alunos.utfpr.edu.br;CDIA
                Bruno Souza;2;bruno@alunos.utfpr.edu.br;
                Carla Lima;3;carla@alunos.utfpr.edu.br;sistemas de informacao
                Davi Rocha;4;davi@alunos.utfpr.edu.br;ADS
                """;

        List<ResultadoEmissaoDTO> relatorio = emissaoService.processarEmissao(lote(csv, 2.0, "CDIA"));

        ArgumentCaptor<EmissaoLoteEventoRequestDTO> dadosPdf = ArgumentCaptor.forClass(EmissaoLoteEventoRequestDTO.class);
        verify(pdfService, times(3)).gerarCertificado(any(), dadosPdf.capture());
        assertEquals(List.of("CDIA", "CDIA", "SI"), dadosPdf.getAllValues().stream().map(EmissaoLoteEventoRequestDTO::cursoPadrao).toList());

        verify(sheetsService).registrarEmissao("Ana Silva", "1", CDIA_CURTO, DATA, EVENTO, 2.0, null);
        verify(sheetsService).registrarEmissao("Bruno Souza", "2", CDIA_CURTO, DATA, EVENTO, 2.0, null);
        verify(sheetsService).registrarEmissao("Carla Lima", "3", SI_CURTO, DATA, EVENTO, 2.0, null);
        assertEquals(new ResultadoEmissaoDTO("Davi Rocha", "davi@alunos.utfpr.edu.br", StatusEmissao.ERRO, "Curso inválido: ADS"),
                relatorio.get(3));
        verify(emailService, never()).enviarCertificado(eq("davi@alunos.utfpr.edu.br"), anyString(), anyString(), any());
    }

    @Test
    void loteSemColunaCursoESemCursoPadraoEmiteComoSi() {
        prepararDrive(Set.of());

        emissaoService.processarEmissao(lote("Nome;RA;E-mail\nAna Silva;1;ana@alunos.utfpr.edu.br\n", 2.0));

        ArgumentCaptor<EmissaoLoteEventoRequestDTO> dadosPdf = ArgumentCaptor.forClass(EmissaoLoteEventoRequestDTO.class);
        verify(pdfService).gerarCertificado(any(), dadosPdf.capture());
        assertEquals("SI", dadosPdf.getValue().cursoPadrao());
        verify(sheetsService).registrarEmissao("Ana Silva", "1", SI_CURTO, DATA, EVENTO, 2.0, null);
    }

    @Test
    void unitarioUsaOCursoDoParticipante() {
        prepararDrive(Set.of());
        EmissaoIndividualRequestDTO dto = new EmissaoIndividualRequestDTO(EVENTO, DATA, null, 2.0,
                "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", "cdia");

        emissaoService.processarEmissaoIndividual(dto);

        ArgumentCaptor<EmissaoLoteEventoRequestDTO> dadosPdf = ArgumentCaptor.forClass(EmissaoLoteEventoRequestDTO.class);
        verify(pdfService).gerarCertificado(any(), dadosPdf.capture());
        assertEquals("CDIA", dadosPdf.getValue().cursoPadrao());
        verify(sheetsService).registrarEmissao("Ana Silva", "12345", CDIA_CURTO, DATA, EVENTO, 2.0, null);
    }

    @Test
    void unitarioSemCursoEmiteComoSi() {
        prepararDrive(Set.of());

        emissaoService.processarEmissaoIndividual(unitario("Ana Silva", "ana@alunos.utfpr.edu.br", 2.0));

        verify(sheetsService).registrarEmissao("Ana Silva", "12345", SI_CURTO, DATA, EVENTO, 2.0, null);
    }

    @Test
    void devePularLinhaComHorasInvalidasNaEmissao() {
        prepararDrive(Set.of());
        String csv = """
                Nome;RA;E-mail;Horas
                Ana Silva;1;ana@alunos.utfpr.edu.br;abc
                Bruno Souza;2;bruno@alunos.utfpr.edu.br;3
                """;

        List<ResultadoEmissaoDTO> relatorio = emissaoService.processarEmissao(lote(csv, 2.0));

        assertEquals(List.of(
                new ResultadoEmissaoDTO("Ana Silva", "ana@alunos.utfpr.edu.br", StatusEmissao.ERRO, "Horas inválidas: abc"),
                new ResultadoEmissaoDTO("Bruno Souza", "bruno@alunos.utfpr.edu.br", StatusEmissao.SUCESSO, null)
        ), relatorio);
        verify(pdfService, times(1)).gerarCertificado(any(), any());
        verify(emailService, never()).enviarCertificado(eq("ana@alunos.utfpr.edu.br"), anyString(), anyString(), any());
        verify(sheetsService).registrarEmissao("Bruno Souza", "2", SI_CURTO, DATA, EVENTO, 3.0, null);
    }

    @Test
    void deveJuntarMensagensDeErroNoRelatorio() {
        List<ResultadoEmissaoDTO> relatorio = emissaoService.processarEmissao(lote("Nome;RA;E-mail\n;1;\n", 2.0));

        assertEquals(List.of(new ResultadoEmissaoDTO("", "", StatusEmissao.ERRO, "Nome não informado; E-mail não informado")), relatorio);
    }

    @Test
    void naoDeveTocarNoDriveQuandoTodasAsLinhasTemErro() {
        List<ResultadoEmissaoDTO> relatorio = emissaoService.processarEmissao(
                lote("Nome;RA;E-mail\nAna;1;ana@utfpr\nBruno;2;\n", 2.0));

        assertEquals(List.of(StatusEmissao.ERRO, StatusEmissao.ERRO), relatorio.stream().map(ResultadoEmissaoDTO::status).toList());
        semEfeitos();
    }

    @Test
    void deveRecusarLoteComEventoInvalidoAntesDeQualquerEfeito() {
        assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissao(lote("Nome;E-mail\nAna;ana@x.com\n", null)));
        semEfeitos();
    }

    @Test
    void deveRecusarLoteComCabecalhoInvalidoAntesDeQualquerEfeito() {
        assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissao(lote("Nome;RA\nAna;1\n", 2.0)));
        semEfeitos();
    }

    // ---------- Emissão unitária (CA6, CA9) ----------

    @Test
    void deveEmitirUnitarioValido() {
        prepararDrive(Set.of());

        ResultadoEmissaoDTO resultado = emissaoService.processarEmissaoIndividual(unitario(" Ana Silva ", " ana@alunos.utfpr.edu.br ", 2.0));

        assertEquals(new ResultadoEmissaoDTO("Ana Silva", "ana@alunos.utfpr.edu.br", StatusEmissao.SUCESSO, null), resultado);
        verify(driveService).fazerUploadCertificado(PDF, "Certificado_Ana_Silva.pdf", PASTA);
    }

    @Test
    void deveRecusarUnitarioComNomeVazioAntesDeQualquerEfeito() {
        assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissaoIndividual(unitario("  ", "ana@alunos.utfpr.edu.br", 2.0)));
        semEfeitos();
    }

    @Test
    void deveRecusarUnitarioComEmailInvalidoAntesDeQualquerEfeito() {
        assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissaoIndividual(unitario("Ana Silva", "ana@utfpr", 2.0)));
        semEfeitos();
    }

    @Test
    void deveRecusarUnitarioComCargaAcimaDoMaximoAntesDeQualquerEfeito() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissaoIndividual(unitario("Ana Silva", "ana@alunos.utfpr.edu.br", 1000.01)));

        assertEquals("A carga horária deve ser de no máximo 1.000 horas.", erro.getMessage());
        semEfeitos();
    }

    @Test
    void deveRecusarLoteComCargaAbaixoDeUmMinutoAntesDeQualquerEfeito() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissao(lote("Nome;E-mail\nAna;ana@x.com\n", 0.001)));

        assertEquals("A carga horária deve ser um número maior que zero.", erro.getMessage());
        semEfeitos();
    }

    @Test
    void deveRecusarUnitarioComEventoInvalidoAntesDeQualquerEfeito() {
        assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissaoIndividual(unitario("Ana Silva", "ana@alunos.utfpr.edu.br", -1.0)));
        semEfeitos();
    }

    // ---------- Data de fim (F3: CA6, CA7) ----------

    @Test
    void deveRecusarPreVisualizacaoComFimAnteriorAoInicioSemEfeitos() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.preVisualizarLote(loteComFim(DATA.minusDays(1))));

        assertEquals("A data de fim não pode ser anterior à data de início.", erro.getMessage());
        semEfeitos();
    }

    @Test
    void deveRecusarLoteComEventoLongoDemaisSemEfeitos() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissao(loteComFim(DATA.plusDays(367))));

        assertEquals("O evento deve durar no máximo 366 dias.", erro.getMessage());
        semEfeitos();
    }

    @Test
    void deveRecusarUnitarioComFimAnteriorAoInicioSemEfeitos() {
        EmissaoIndividualRequestDTO dto = new EmissaoIndividualRequestDTO(EVENTO, DATA, DATA.minusDays(1), 2.0,
                "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", null);

        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> emissaoService.processarEmissaoIndividual(dto));

        assertEquals("A data de fim não pode ser anterior à data de início.", erro.getMessage());
        semEfeitos();
    }

    // ---------- Sheets com data de fim (F3: CA10) ----------

    @Test
    void loteComFimRegistraADataDeFimNoSheets() {
        prepararDrive(Set.of());

        emissaoService.processarEmissao(loteComFim(DATA.plusDays(4)));

        verify(sheetsService).registrarEmissao("Ana Silva", null, SI_CURTO, DATA, EVENTO, 2.0, DATA.plusDays(4));
    }

    @Test
    void unitarioComFimRegistraADataDeFimNoSheets() {
        prepararDrive(Set.of());
        EmissaoIndividualRequestDTO dto = new EmissaoIndividualRequestDTO(EVENTO, DATA, DATA.plusDays(4), 2.0,
                "Ana Silva", "ana@alunos.utfpr.edu.br", "12345", null);

        emissaoService.processarEmissaoIndividual(dto);

        verify(sheetsService).registrarEmissao("Ana Silva", "12345", SI_CURTO, DATA, EVENTO, 2.0, DATA.plusDays(4));
    }

    private EmissaoLoteEventoRequestDTO loteComFim(LocalDate dataFim) {
        MockMultipartFile arquivo = new MockMultipartFile("arquivoCsv", "participantes.csv", "text/csv",
                "Nome;E-mail\nAna Silva;ana@alunos.utfpr.edu.br\n".getBytes(StandardCharsets.UTF_8));
        return new EmissaoLoteEventoRequestDTO(EVENTO, DATA, dataFim, 2.0, null, arquivo);
    }

    private void prepararDrive(Set<String> existentes) {
        when(driveService.obterOuCriarPastaEvento(EVENTO, DATA)).thenReturn(PASTA);
        when(driveService.listarArquivosNaPasta(PASTA)).thenReturn(existentes);
        when(pdfService.gerarCertificado(any(), any())).thenReturn(PDF);
    }

    private void semEfeitos() {
        verifyNoInteractions(pdfService, emailService, driveService, sheetsService);
    }

    private EmissaoLoteEventoRequestDTO lote(String csv, Double carga) {
        return lote(csv, carga, null);
    }

    private EmissaoLoteEventoRequestDTO lote(String csv, Double carga, String cursoPadrao) {
        MockMultipartFile arquivo = new MockMultipartFile("arquivoCsv", "participantes.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));
        return new EmissaoLoteEventoRequestDTO(EVENTO, DATA, null, carga, cursoPadrao, arquivo);
    }

    private EmissaoIndividualRequestDTO unitario(String nome, String email, Double carga) {
        return new EmissaoIndividualRequestDTO(EVENTO, DATA, null, carga, nome, email, "12345", null);
    }
}
