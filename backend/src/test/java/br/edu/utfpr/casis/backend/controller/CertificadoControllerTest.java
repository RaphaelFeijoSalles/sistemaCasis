package br.edu.utfpr.casis.backend.controller;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.dto.LinhaValidadaDTO;
import br.edu.utfpr.casis.backend.dto.PreVisualizacaoLoteDTO;
import br.edu.utfpr.casis.backend.dto.ResultadoEmissaoDTO;
import br.edu.utfpr.casis.backend.dto.SituacaoLinha;
import br.edu.utfpr.casis.backend.dto.StatusEmissao;
import br.edu.utfpr.casis.backend.service.DadosInvalidosException;
import br.edu.utfpr.casis.backend.service.EmissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CertificadoController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "API_SECRET=chave-teste")
class CertificadoControllerTest {

    private static final String CHAVE = "chave-teste";
    private static final String BASE = "/api/certificados";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmissaoService emissaoService;

    private final MockMultipartFile csv = new MockMultipartFile("arquivoCsv", "participantes.csv", "text/csv",
            "Nome;E-mail\nAna;ana@alunos.utfpr.edu.br\n".getBytes(StandardCharsets.UTF_8));

    // ---------- /pre-visualizar-lote ----------

    @Test
    void devePreVisualizarLoteComCamposDoFormData() throws Exception {
        LinhaValidadaDTO linha = new LinhaValidadaDTO(2, "Ana", "ana@alunos.utfpr.edu.br", null,
                1.5, "1 hora e 30 minutos", true, "SI", "Bacharelado em Sistemas de Informação", true,
                SituacaoLinha.AVISO, List.of("RA não informado"));
        when(emissaoService.preVisualizarLote(any())).thenReturn(new PreVisualizacaoLoteDTO(1, 0, 1, 0, List.of(linha)));

        mockMvc.perform(multipart(BASE + "/pre-visualizar-lote").file(csv)
                        .param("nomeEvento", "Semana Acadêmica")
                        .param("dataRealizacao", "2026-11-20")
                        .param("cargaHoraria", "1.5")
                        .header("X-API-KEY", CHAVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinhas").value(1))
                .andExpect(jsonPath("$.comAviso").value(1))
                .andExpect(jsonPath("$.linhas[0].linha").value(2))
                .andExpect(jsonPath("$.linhas[0].situacao").value("AVISO"))
                .andExpect(jsonPath("$.linhas[0].mensagens[0]").value("RA não informado"))
                .andExpect(jsonPath("$.linhas[0].horas").value(1.5))
                .andExpect(jsonPath("$.linhas[0].horasTexto").value("1 hora e 30 minutos"))
                .andExpect(jsonPath("$.linhas[0].horasDoFormulario").value(true));

        verify(emissaoService).preVisualizarLote(argThat((EmissaoLoteEventoRequestDTO dto) ->
                dto.nomeEvento().equals("Semana Acadêmica")
                        && dto.dataRealizacao().equals(LocalDate.of(2026, 11, 20))
                        && dto.cargaHoraria().equals(1.5)
                        && dto.arquivoCsv() != null));
    }

    @Test
    void deveRecusarPreVisualizacaoSemChave() throws Exception {
        mockMvc.perform(multipart(BASE + "/pre-visualizar-lote").file(csv).header("X-API-KEY", "errada"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(emissaoService);
    }

    @Test
    void deveRecusarPreVisualizacaoSemArquivo() throws Exception {
        mockMvc.perform(multipart(BASE + "/pre-visualizar-lote").param("nomeEvento", "Semana").header("X-API-KEY", CHAVE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro: O arquivo CSV é obrigatório."));

        verifyNoInteractions(emissaoService);
    }

    @Test
    void deveDevolver400QuandoPreVisualizacaoTemDadosInvalidos() throws Exception {
        when(emissaoService.preVisualizarLote(any())).thenThrow(new DadosInvalidosException("Cabeçalho inválido."));

        mockMvc.perform(multipart(BASE + "/pre-visualizar-lote").file(csv).header("X-API-KEY", CHAVE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cabeçalho inválido."));
    }

    // ---------- /emitir-lote ----------

    @Test
    void deveEmitirLote() throws Exception {
        when(emissaoService.processarEmissao(any())).thenReturn(List.of(
                new ResultadoEmissaoDTO("Ana", "ana@alunos.utfpr.edu.br", StatusEmissao.SUCESSO, null)));

        mockMvc.perform(multipart(BASE + "/emitir-lote").file(csv).header("X-API-KEY", CHAVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCESSO"));
    }

    @Test
    void deveDevolver400QuandoLoteTemDadosInvalidos() throws Exception {
        when(emissaoService.processarEmissao(any())).thenThrow(new DadosInvalidosException("A carga horária deve ser um número maior que zero."));

        mockMvc.perform(multipart(BASE + "/emitir-lote").file(csv).header("X-API-KEY", CHAVE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("A carga horária deve ser um número maior que zero."));
    }

    @Test
    void deveManter500ParaFalhaInesperadaNoLote() throws Exception {
        when(emissaoService.processarEmissao(any())).thenThrow(new IllegalStateException("Drive fora do ar"));

        mockMvc.perform(multipart(BASE + "/emitir-lote").file(csv).header("X-API-KEY", CHAVE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro crítico: Drive fora do ar"));
    }

    @Test
    void deveDevolver400ComMensagemDeCargaAcimaDoMaximo() throws Exception {
        when(emissaoService.preVisualizarLote(any())).thenThrow(new DadosInvalidosException("A carga horária deve ser de no máximo 1.000 horas."));

        mockMvc.perform(multipart(BASE + "/pre-visualizar-lote").file(csv)
                        .param("nomeEvento", "Semana Acadêmica")
                        .param("dataRealizacao", "2026-11-20")
                        .param("cargaHoraria", "1000.01")
                        .header("X-API-KEY", CHAVE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("A carga horária deve ser de no máximo 1.000 horas."));
    }

    // ---------- /emitir-unitario ----------

    @Test
    void deveDevolver400QuandoUnitarioTemDadosInvalidos() throws Exception {
        when(emissaoService.processarEmissaoIndividual(any())).thenThrow(new DadosInvalidosException("E-mail do participante inválido: ana@utfpr"));

        mockMvc.perform(post(BASE + "/emitir-unitario")
                        .header("X-API-KEY", CHAVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomeEvento":"Semana","dataRealizacao":"2026-11-20","cargaHoraria":"2",
                                 "nomeParticipante":"Ana","emailParticipante":"ana@utfpr","raParticipante":"1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("E-mail do participante inválido: ana@utfpr"));
    }

    @Test
    void deveRecusarUnitarioSemChave() throws Exception {
        mockMvc.perform(post(BASE + "/emitir-unitario").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(emissaoService);
    }
}
