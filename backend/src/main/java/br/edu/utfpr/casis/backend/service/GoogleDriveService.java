package br.edu.utfpr.casis.backend.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Classe responsável por cuidar de tudo que envolve o GoogleDrive.
 * Motivo principal: ter controle da geração/backup dos certificados.
 */
@Slf4j
@Service
public class GoogleDriveService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GOOGLE_REFRESH_TOKEN}")
    private String refreshToken;

    @Value("${google.drive.pasta.raiz.id}")
    private String pastaRaizId;

    private Drive obterServico() throws Exception {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh Token ausente no ambiente.");
        }

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("CASIS Certificados")
                .build();
    }

    /**
     * Retorna a pasta: cria se não existir, simplesmente retorna se já existir.
     */
    public String obterOuCriarPastaEvento(String nomeEvento, LocalDate dataEvento) {
        try {
            Drive driveService = obterServico();

            String nomeLimpo = nomeEvento != null ? nomeEvento.trim() : "Evento";
            int ano = dataEvento.getYear();
            int semestre = dataEvento.getMonthValue() <= 6 ? 1 : 2;

            // Exemplo: "Recepção de Calouros 2026/1"
            String nomePasta = String.format("%s %d/%d", nomeLimpo, ano, semestre);
            String nomePesquisa = nomePasta.replace("'", "\\'");

            String query = String.format("mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents and trashed=false",
                    nomePesquisa, pastaRaizId);

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .execute();

            if (!result.getFiles().isEmpty()) {
                log.info("Pasta do evento '{}' encontrada no Drive.", nomePasta);
                return result.getFiles().getFirst().getId();
            }

            File fileMetadata = new File();
            fileMetadata.setName(nomePasta);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            fileMetadata.setParents(Collections.singletonList(pastaRaizId));

            File folder = driveService.files().create(fileMetadata).setFields("id").execute();
            log.info("Nova pasta '{}' criada no Drive com sucesso.", nomePasta);

            return folder.getId();

        } catch (Exception e) {
            log.error("Erro ao gerir pasta no Google Drive", e);
            throw new RuntimeException("Falha na comunicação com o Google Drive.", e);
        }
    }

    /**
     * Retorna um Set com todos os nomes de arquivos dentro de uma pasta para busca rápida.
     */
    public Set<String> listarArquivosNaPasta(String folderId) {
        try {
            Drive driveService = obterServico();
            String query = String.format("'%s' in parents and trashed=false", folderId);

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setFields("files(name)")
                    .execute();

            return result.getFiles().stream()
                    .map(File::getName)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Erro ao listar arquivos da pasta {}", folderId);
            return Collections.emptySet();
        }
    }

    /**
     * Efetua o upload do certificado para a pasta correta
     */
    public void fazerUploadCertificado(byte[] pdfBytes, String nomeFicheiro, String folderId) {
        try {
            Drive driveService = obterServico();
            File fileMetadata = new File();
            fileMetadata.setName(nomeFicheiro);
            fileMetadata.setParents(Collections.singletonList(folderId));

            InputStreamContent mediaContent = new InputStreamContent(
                    "application/pdf",
                    new ByteArrayInputStream(pdfBytes)
            );

            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            log.info("Upload de {} concluído.", nomeFicheiro);
        } catch (Exception e) {
            log.error("Erro ao enviar o ficheiro {} para o Drive.", nomeFicheiro, e);
        }
    }
}