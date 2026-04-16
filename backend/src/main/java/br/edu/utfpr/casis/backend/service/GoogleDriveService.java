package br.edu.utfpr.casis.backend.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collections;

@Slf4j
@Service
public class GoogleDriveService {

    // A string Base64
    @Value("${GOOGLE_CREDENTIALS_BASE64:}")
    private String base64Credentials;

    // LÊ DINAMICAMENTE DA CONFIGURAÇÃO (Remove o "final" e o link hardcoded)
    @Value("${google.drive.pasta.raiz.id}")
    private String pastaRaizId;

    private Drive obterServico() throws Exception {
        if (base64Credentials == null || base64Credentials.trim().isEmpty() || base64Credentials.equals("colar_tua_base64_aqui")) {
            throw new IllegalArgumentException("Credenciais do Google Drive ausentes no ambiente local.");
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes))
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("CASIS Certificados")
                .build();
    }

    public String obterOuCriarPastaEvento(String nomeEvento) {
        try {
            Drive driveService = obterServico();

            // Agora usa a variável "pastaRaizId" injetada
            String query = String.format("mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents and trashed=false",
                    nomeEvento, pastaRaizId);

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .execute();

            if (!result.getFiles().isEmpty()) {
                log.info("Pasta do evento '{}' encontrada no Drive.", nomeEvento);
                return result.getFiles().getFirst().getId();
            }

            // Se não encontrou, cria a nova pasta
            File fileMetadata = new File();
            fileMetadata.setName(nomeEvento);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            fileMetadata.setParents(Collections.singletonList(pastaRaizId));

            File folder = driveService.files().create(fileMetadata).setFields("id").execute();
            log.info("Nova pasta '{}' criada no Drive com sucesso.", nomeEvento);

            return folder.getId();

        } catch (Exception e) {
            log.error("Erro ao gerir pasta no Google Drive", e);
            throw new RuntimeException("Falha na comunicação com o Google Drive.", e);
        }
    }

    /**
     * Efetua o upload do PDF gerado para a pasta específica do evento.
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