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
     * Adiciona uma nova linha à planilha do Google Sheets com os dados do certificado emitido.
     *
     * @param nome Nome do participante.
     * @param ra Registro Acadêmico do participante.
     * @param curso Curso do participante (ex: Sistemas de Informação).
     * @param dataEvento Data de realização do evento.
     * @param evento Nome do evento.
     * @param cargaHoraria Carga horária do evento.
     */
    public void registrarEmissao(String nome, String ra, String curso, LocalDate dataEvento, String evento, Integer cargaHoraria) {
        log.debug("Iniciando registro na planilha para o aluno: {}", nome);
        
        try {
            Sheets sheetsService = obterServico();
            
            // Formatar a data para o padrão brasileiro
            String dataFormatada = dataEvento != null ? dataEvento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            
            // Tratar valores nulos (Fallback para curso padrão caso venha nulo)
            String raFinal = (ra != null && !ra.isEmpty()) ? ra : "N/A";
            String cursoFinal = (curso != null && !curso.isEmpty()) ? curso : "Sistemas de Informação"; 

            // Mapeia as colunas nesta exata ordem: nome | ra | curso | data_evento | evento | carga_horaria
            List<Object> rowData = Arrays.asList(
                    nome,
                    raFinal,
                    cursoFinal,
                    dataFormatada,
                    evento,
                    cargaHoraria
            );

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