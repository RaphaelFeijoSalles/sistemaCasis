package br.edu.utfpr.casis.backend.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Serviço responsável pela integração com a API do Google Sheets.
 * Gerencia a inserção de registros (linhas) de emissão de certificados
 * em uma planilha de controle centralizada.
 */
@Slf4j
@Service
public class GoogleSheetsService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GOOGLE_REFRESH_TOKEN}")
    private String refreshToken;

    @Value("${GOOGLE_SHEETS_PLANILHA_ID}")
    private String planilhaId;
    
    @Value("${GOOGLE_SHEETS_NOME_ABA:Página1}")
    private String nomeAba;

    private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Inicializa e retorna o serviço de comunicação com o Google Sheets utilizando
     * as credenciais OAuth 2.0 (Client ID, Client Secret e Refresh Token).
     *
     * @return Uma instância configurada de {@link Sheets}.
     * @throws Exception Se ocorrer erro na autenticação ou construção do serviço.
     * @throws IllegalArgumentException Se o Refresh Token não estiver configurado.
     */
    private Sheets obterServico() throws Exception {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh Token ausente no ambiente. Verifique as configurações (secrets.properties).");
        }

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Sheets.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("CASIS Certificados")
                .build();
    }

    /**
     * Valor da coluna {@code data_fim} (F3): vazio para evento de um dia (fim ausente ou igual ao início),
     * como no certificado; senão, a data de fim em {@code dd/MM/yyyy}.
     *
     * @param dataEvento data de início do evento.
     * @param dataFim data de fim do evento, ou {@code null}.
     * @return a data de fim formatada, ou {@code ""} para evento de um dia.
     */
    static String formatarDataFim(LocalDate dataEvento, LocalDate dataFim) {
        if (dataFim == null || dataFim.equals(dataEvento)) {
            return "";
        }
        return dataFim.format(DIA_MES_ANO);
    }

    /**
     * Monta a linha da planilha na ordem {@code nome | ra | curso | data | evento | carga_horaria | data_fim}.
     * <p>
     * Participante externo (F8, {@link RegistroAcademico#ehExterno(String)}): a coluna {@code ra} recebe
     * {@link RegistroAcademico#EXTERNO} e a coluna {@code curso} fica vazia, qualquer que seja o curso recebido.
     * Para os demais, RA vazio vira {@code "N/A"} e curso vazio vira "Sistemas de Informação" (defesa mantida).
     *
     * @param nome Nome do participante.
     * @param ra Registro Acadêmico do participante, ou o marcador de externo.
     * @param curso Curso do participante; ignorado para externo.
     * @param dataEvento Data de início do evento.
     * @param evento Nome do evento.
     * @param cargaHoraria Carga horária em horas decimais.
     * @param dataFim Data de fim do evento, ou {@code null}.
     * @return a linha com as 7 colunas da planilha.
     */
    static List<Object> montarLinha(String nome, String ra, String curso, LocalDate dataEvento, String evento,
                                    Double cargaHoraria, LocalDate dataFim) {
        // Formatar a data para o padrão brasileiro
        String dataFormatada = dataEvento != null ? dataEvento.format(DIA_MES_ANO) : "";
        String dataFimFormatada = formatarDataFim(dataEvento, dataFim);

        String raFinal;
        String cursoFinal;
        if (RegistroAcademico.ehExterno(ra)) {
            // Externo (F8): marcador normalizado e sem curso
            raFinal = RegistroAcademico.EXTERNO;
            cursoFinal = "";
        } else {
            // Tratar valores nulos (Fallback para curso padrão caso venha nulo)
            raFinal = (ra != null && !ra.isEmpty()) ? ra : "N/A";
            cursoFinal = (curso != null && !curso.isEmpty()) ? curso : "Sistemas de Informação";
        }

        String cargaHorariaFormatada = CargaHorariaFormatter.formatarDuracaoPlanilha(cargaHoraria);

        // Mapeia as colunas nesta exata ordem: nome | ra | curso | data_evento | evento | carga_horaria | data_fim (F3)
        return Arrays.asList(
                nome,
                raFinal,
                cursoFinal,
                dataFormatada,
                evento,
                cargaHorariaFormatada,
                dataFimFormatada
        );
    }

    /**
     * Adiciona uma nova linha à planilha do Google Sheets com os dados do certificado emitido.
     *
     * @param nome Nome do participante.
     * @param ra Registro Acadêmico do participante.
     * @param curso Curso do participante (ex: Sistemas de Informação).
     * @param dataEvento Data de início do evento.
     * @param evento Nome do evento.
     * @param cargaHoraria Carga horária do evento.
     * @param dataFim Data de fim do evento (F3); {@code null} ou igual ao início deixa a coluna {@code data_fim} vazia.
     * A linha é montada por {@link #montarLinha}: participante externo (F8) fica com {@code ra = EXTERNO} e curso vazio.
     */
    public void registrarEmissao(String nome, String ra, String curso, LocalDate dataEvento, String evento, Double cargaHoraria,
                                 LocalDate dataFim) {
        log.debug("Iniciando registro na planilha para o aluno: {}", nome);
        
        try {
            Sheets sheetsService = obterServico();

            List<Object> rowData = montarLinha(nome, ra, curso, dataEvento, evento, cargaHoraria, dataFim);

            ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));

            sheetsService.spreadsheets().values()
                    .append(planilhaId, nomeAba, body)
                    .setValueInputOption("USER_ENTERED") // Formata as datas/números nativamente no Google
                    .execute();

            log.info("Emissão de {} registrada com sucesso no Google Sheets.", nome);
            
        } catch (Exception e) {
            log.error("Erro ao registrar a emissão na planilha do Google Sheets para: {}. O certificado ainda será enviado.", nome, e);
            // Capturamos a exception mas NÃO relançamos, pois não queremos cancelar o envio do e-mail só porque a planilha falhou.
        }
    }
}
