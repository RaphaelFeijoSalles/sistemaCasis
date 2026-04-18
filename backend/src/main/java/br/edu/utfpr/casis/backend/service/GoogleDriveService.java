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
     * Orquestra a criação da estrutura de pastas em dois níveis:
     * Nível 1: Pasta do Semestre (Ex: "2026-1") dentro da raiz.
     * Nível 2: Pasta do Evento (Ex: "Apadrinhamento") dentro do semestre.
     */
    public String obterOuCriarPastaEvento(String nomeEvento, LocalDate dataEvento) {
        try {
            Drive driveService = obterServico();

            int ano = dataEvento.getYear();
            int semestre = dataEvento.getMonthValue() <= 6 ? 1 : 2;

            // 1. Define os nomes exatos das pastas
            String nomePastaSemestre = String.format("%d-%d", ano, semestre);
            String nomePastaEvento = nomeEvento != null ? nomeEvento.trim() : "Evento";

            // 2. Garante que a pasta "Ano-Semestre" (Ex: 2026-1) existe na RAIZ
            String idPastaSemestre = obterOuCriarSubpasta(driveService, nomePastaSemestre, pastaRaizId);

            // 3. Garante que a pasta "Evento" (Ex: Apadrinhamento) existe DENTRO do Semestre

            // Retorna o ID da pasta final onde os PDFs serão salvos
            return obterOuCriarSubpasta(driveService, nomePastaEvento, idPastaSemestre);

        } catch (Exception e) {
            log.error("Erro ao gerir a estrutura de pastas no Google Drive", e);
            throw new RuntimeException("Falha na comunicação com o Google Drive.", e);
        }
    }

    /**
     * Método auxiliar genérico que busca ou cria uma pasta dentro de um ID pai (parent) específico.
     * Isso evita repetição de código e nos permite criar N sub-níveis no futuro.
     */
    private String obterOuCriarSubpasta(Drive driveService, String nomePasta, String parentId) throws Exception {
        // Escapa aspas simples para não quebrar a Query
        String nomePesquisa = nomePasta.replace("'", "\\'");

        // Query: Procura uma pasta com este nome exato E que seja filha deste parentId específico
        String query = String.format("mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents and trashed=false",
                nomePesquisa, parentId);

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .execute();

        // Se encontrou, apenas devolve o ID existente
        if (!result.getFiles().isEmpty()) {
            log.info("Pasta '{}' encontrada no Drive.", nomePasta);
            return result.getFiles().getFirst().getId();
        }

        // Se não encontrou, cria a pasta aninhada
        File fileMetadata = new File();
        fileMetadata.setName(nomePasta);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList(parentId));

        File folder = driveService.files().create(fileMetadata).setFields("id").execute();
        log.info("Nova subpasta '{}' criada no Drive com sucesso.", nomePasta);

        return folder.getId();
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
     * Efetua o upload do certificado em PDF para a pasta correta.
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