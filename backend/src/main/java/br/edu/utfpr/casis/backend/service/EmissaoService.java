package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoIndividualRequestDTO;
import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.dto.ResultadoEmissaoDTO;
import br.edu.utfpr.casis.backend.dto.StatusEmissao;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.service.strategy.FonteDadosCertificadoStrategy;
import br.edu.utfpr.casis.backend.service.strategy.FonteDadosCsvStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Serviço central responsável por orquestrar todo o processo de emissão de certificados.
 * Coordena a extração de dados, geração do PDF, envio por e-mail e backup no Google Drive.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmissaoService {

    private final CsvService csvService;
    private final PdfService pdfService;
    private final EmailService emailService;
    private final GoogleDriveService driveService;
    private final GoogleSheetsService sheetsService;

    /**
     * Orquestra o fluxo de emissão de certificados em lote para um evento.
     *
     * @param requestDTO DTO contendo os dados do evento e a fonte de dados (CSV).
     * @return Uma lista de resultados (relatório) contendo o status da emissão para cada participante.
     */
    public List<ResultadoEmissaoDTO> processarEmissao(EmissaoLoteEventoRequestDTO requestDTO) {

        FonteDadosCertificadoStrategy fonteDados = definirEstrategia(requestDTO);
        List<AlunoCertificado> alunos = fonteDados.obterAlunos();
        List<ResultadoEmissaoDTO> relatorio = new ArrayList<>();

        // 1. Resolve a pasta do Google Drive com o Nome e a Data para padronizar Ano/Semestre
        String folderId = driveService.obterOuCriarPastaEvento(requestDTO.nomeEvento(), requestDTO.dataRealizacao());

        // 2. FAZ A VARREDURA UMA ÚNICA VEZ para saber o que já existe lá dentro
        Set<String> arquivosExistentes = driveService.listarArquivosNaPasta(folderId);

        log.info("Iniciando geração para {} certificados. {} arquivos já existem na pasta.", alunos.size(), arquivosExistentes.size());

        for (AlunoCertificado aluno : alunos) {
            // Limpa o nome para padronizar o nome do arquivo
            String nomePdf = "Certificado_" + aluno.getNome().trim().replaceAll("\\s+", "_") + ".pdf";

            // 3. VERIFICAÇÃO DE DUPLICIDADE: Se já existe no Drive, pula o processo inteiro!
            if (arquivosExistentes.contains(nomePdf)) {
                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), StatusEmissao.EXISTENTE, "Certificado já consta no Drive."));
                continue;
            }

            try {
                // Gera, Envia e faz Upload apenas se for novo
                byte[] pdf = pdfService.gerarCertificado(aluno, requestDTO);
                emailService.enviarCertificado(aluno.getEmail(), aluno.getNome(), requestDTO.nomeEvento(), pdf);
                driveService.fazerUploadCertificado(pdf, nomePdf, folderId);
                
                // Registra na planilha de controle
                sheetsService.registrarEmissao(aluno.getNome(), aluno.getRa(), null, requestDTO.dataRealizacao(), requestDTO.nomeEvento(), requestDTO.cargaHoraria());

                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), StatusEmissao.SUCESSO, null));

            } catch (Exception e) {
                log.error("Falha ao emitir para o aluno: {}", aluno.getEmail(), e);
                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), StatusEmissao.ERRO, e.getMessage()));
            }
        }

        return relatorio;
    }

    /**
     * Processa a emissão de um certificado individual (unitário).
     *
     * @param dto DTO contendo os dados do evento e do participante único.
     * @return O resultado detalhado da emissão.
     */
    public ResultadoEmissaoDTO processarEmissaoIndividual(EmissaoIndividualRequestDTO dto) {
        // 1. Cria o aluno manualmente
        AlunoCertificado aluno = br.edu.utfpr.casis.backend.model.AlunoCertificadoEvento.builder()
                .nome(dto.nomeParticipante().trim())
                .email(dto.emailParticipante().trim())
                .ra(dto.raParticipante())
                .build();

        // 2. Cria um DTO de lote "falso" (sem arquivo) só para o PdfService aceitar os dados do evento
        EmissaoLoteEventoRequestDTO loteDto = new EmissaoLoteEventoRequestDTO(
                dto.nomeEvento(), dto.dataRealizacao(), dto.cargaHoraria(), null
        );

        // 3. Resolve a pasta e checa se já existe (reaproveitando a lógica)
        String folderId = driveService.obterOuCriarPastaEvento(dto.nomeEvento(), dto.dataRealizacao());
        String nomePdf = "Certificado_" + aluno.getNome().trim().replaceAll("\\s+", "_") + ".pdf";

        java.util.Set<String> arquivosExistentes = driveService.listarArquivosNaPasta(folderId);
        if (arquivosExistentes.contains(nomePdf)) {
            return new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), br.edu.utfpr.casis.backend.dto.StatusEmissao.EXISTENTE, "Certificado já consta no Drive.");
        }

        // 4. Processo normal de emissão
        try {
            byte[] pdf = pdfService.gerarCertificado(aluno, loteDto);
            emailService.enviarCertificado(aluno.getEmail(), aluno.getNome(), dto.nomeEvento(), pdf);
            driveService.fazerUploadCertificado(pdf, nomePdf, folderId);
            
            // Registra na planilha de controle
            sheetsService.registrarEmissao(aluno.getNome(), aluno.getRa(), null, dto.dataRealizacao(), dto.nomeEvento(), dto.cargaHoraria());

            return new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), br.edu.utfpr.casis.backend.dto.StatusEmissao.SUCESSO, null);
        } catch (Exception e) {
            log.error("Falha na emissão individual para: {}", aluno.getEmail(), e);
            return new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), br.edu.utfpr.casis.backend.dto.StatusEmissao.ERRO, e.getMessage());
        }
    }

    /**
     * Factory Method interno: Define qual estratégia de fonte de dados utilizar.
     * Atualmente, se um CSV for fornecido, utiliza a estratégia CSV.
     * Em versões futuras, poderá identificar se os dados vêm do Banco de Dados.
     *
     * @param dto DTO contendo a possível fonte de dados.
     * @return A implementação concreta de {@link FonteDadosCertificadoStrategy}.
     */
    private FonteDadosCertificadoStrategy definirEstrategia(EmissaoLoteEventoRequestDTO dto) {
        // Se veio um arquivo, usamos a estratégia CSV (Fase 1: Eventos)
        if (dto.arquivoCsv() != null && !dto.arquivoCsv().isEmpty()) {
            return new FonteDadosCsvStrategy(dto.arquivoCsv(), csvService);
        }

        // TODO: Fase 2 - Se não veio arquivo mas veio um "ID de Diretoria", retornar FonteDadosBancoStrategy()

        throw new IllegalArgumentException("Nenhuma fonte de dados válida (CSV ou Banco) encontrada na requisição.");
    }
}