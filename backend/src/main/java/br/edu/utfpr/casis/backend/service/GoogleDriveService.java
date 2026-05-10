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
 * Serviço responsável pela integração com a API do Google Drive.
 * Gerencia a criação de pastas estruturadas (Ano-Semestre/Evento), listagem de arquivos
 * para verificação de duplicidade e upload dos certificados gerados.
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

    @Value("${GOOGLE_DRIVE_PASTA_RAIZ_ID}")
    private String pastaRaizId;

    /**
     * Inicializa e retorna o serviço de comunicação com o Google Drive utilizando
     * as credenciais OAuth 2.0 (Client ID, Client Secret e Refresh Token).
     *
     * @return Uma instância configurada de {@link Drive}.
     * @throws Exception Se ocorrer erro na autenticação ou construção do serviço.
     * @throws IllegalArgumentException Se o Refresh Token não estiver configurado.
     */
    private Drive obterServico() throws Exception {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh Token ausente no ambiente. Verifique as configurações (secrets.properties).");
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
     * Orquestra a criação da estrutura de pastas no Google Drive em dois níveis hierárquicos:
     * Nível 1: Pasta do Semestre (Ex: "2026-1") criada dentro da pasta raiz configurada.
     * Nível 2: Pasta do Evento (Ex: "Apadrinhamento") criada dentro da pasta do semestre correspondente.
     *
     * @param nomeEvento Nome do evento, que será usado como nome da subpasta final.
     * @param dataEvento Data do evento, usada para calcular o ano e semestre.
     * @return O ID da pasta final (do evento) onde os PDFs deverão ser salvos.
     * @throws RuntimeException Se houver falha na comunicação com o Google Drive.
     */
    public String obterOuCriarPastaEvento(String nomeEvento, LocalDate dataEvento) {
        log.info("Resolvendo estrutura de pastas para o evento '{}' ocorrido em {}", nomeEvento, dataEvento);
        try {
            Drive driveService = obterServico();

            int ano = dataEvento.getYear();
            int semestre = dataEvento.getMonthValue() <= 6 ? 1 : 2;

            // 1. Define os nomes exatos das pastas
            String nomePastaSemestre = String.format("%d-%d", ano, semestre);
            String nomePastaEvento = nomeEvento != null && !nomeEvento.trim().isEmpty() ? nomeEvento.trim() : "Evento_Sem_Nome";

            // 2. Garante que a pasta "Ano-Semestre" (Ex: 2026-1) existe na RAIZ
            String idPastaSemestre = obterOuCriarSubpasta(driveService, nomePastaSemestre, pastaRaizId);

            // 3. Garante que a pasta "Evento" (Ex: Apadrinhamento) existe DENTRO do Semestre
            // Retorna o ID da pasta final onde os PDFs serão salvos
            String idFinal = obterOuCriarSubpasta(driveService, nomePastaEvento, idPastaSemestre);
            log.info("ID da pasta de destino resolvido com sucesso: {}", idFinal);
            return idFinal;

        } catch (Exception e) {
            log.error("Erro crítico ao gerir a estrutura de pastas no Google Drive", e);
            throw new RuntimeException("Falha na comunicação com o Google Drive.", e);
        }
    }

    /**
     * Método auxiliar genérico que busca uma pasta pelo nome exato dentro de um ID pai específico.
     * Se a pasta não existir, ela é criada. Isso permite a criação de infinitos sub-níveis, se necessário.
     *
     * @param driveService Serviço instanciado do Google Drive API.
     * @param nomePasta Nome da subpasta a ser procurada ou criada.
     * @param parentId ID da pasta pai onde a subpasta deve estar contida.
     * @return O ID da subpasta (encontrada ou recém-criada).
     * @throws Exception Em caso de erros de API do Google.
     */
    private String obterOuCriarSubpasta(Drive driveService, String nomePasta, String parentId) throws Exception {
        // Escapa aspas simples para não quebrar a Query de busca no Drive
        String nomePesquisa = nomePasta.replace("'", "\\'");

        // Query: Procura uma pasta com este nome exato E que seja filha deste parentId específico (não pode estar na lixeira)
        String query = String.format("mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents and trashed=false",
                nomePesquisa, parentId);

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .execute();

        // Se encontrou, apenas devolve o ID existente
        if (!result.getFiles().isEmpty()) {
            log.debug("Pasta '{}' encontrada no Drive.", nomePasta);
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
     * Lista o nome de todos os arquivos contidos em uma pasta específica do Google Drive.
     * Utilizado para a mecânica de anti-duplicação (fallback em memória) antes da geração do PDF.
     *
     * @param folderId O ID da pasta cujos arquivos devem ser listados.
     * @return Um conjunto (Set) contendo os nomes exatos de todos os arquivos encontrados na pasta.
     */
    public Set<String> listarArquivosNaPasta(String folderId) {
        log.info("Listando arquivos existentes na pasta ID: {} para verificação de duplicidade.", folderId);
        try {
            Drive driveService = obterServico();
            String query = String.format("'%s' in parents and trashed=false", folderId);

            FileList result = driveService.files().list()
                    .setQ(query)
                    .setFields("files(name)")
                    .execute();

            Set<String> arquivos = result.getFiles().stream()
                    .map(File::getName)
                    .collect(Collectors.toSet());
            
            log.debug("{} arquivos encontrados na pasta.", arquivos.size());
            return arquivos;
        } catch (Exception e) {
            log.error("Erro ao listar arquivos da pasta {}. A verificação de duplicidade pode falhar.", folderId, e);
            return Collections.emptySet();
        }
    }

    /**
     * Efetua o upload de um certificado em formato PDF para uma pasta específica do Google Drive.
     *
     * @param pdfBytes Array de bytes contendo o conteúdo do arquivo PDF.
     * @param nomeFicheiro Nome final que o arquivo terá no Google Drive (Ex: "Certificado_Joao_Silva.pdf").
     * @param folderId O ID da pasta de destino no Google Drive.
     */
    public void fazerUploadCertificado(byte[] pdfBytes, String nomeFicheiro, String folderId) {
        log.debug("Iniciando upload do arquivo '{}' para o Drive.", nomeFicheiro);
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

            log.info("Upload de '{}' concluído com sucesso no Drive.", nomeFicheiro);
        } catch (Exception e) {
            log.error("Erro ao enviar o ficheiro '{}' para o Drive. Verifique a conexão e permissões.", nomeFicheiro, e);
        }
    }
}