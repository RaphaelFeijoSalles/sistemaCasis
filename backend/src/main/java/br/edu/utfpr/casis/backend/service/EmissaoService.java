package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.dto.ResultadoEmissaoDTO;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.service.strategy.FonteDadosCertificadoStrategy;
import br.edu.utfpr.casis.backend.service.strategy.FonteDadosCsvStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmissaoService {

    private final CsvService csvService;
    private final PdfService pdfService;
    private final EmailService emailService;
    private final GoogleDriveService driveService;

    /**
     * Orquestra o fluxo de extração, geração e envio.
     */


    public List<ResultadoEmissaoDTO> processarEmissao(EmissaoLoteEventoRequestDTO requestDTO) {

        FonteDadosCertificadoStrategy fonteDados = definirEstrategia(requestDTO);
        List<AlunoCertificado> alunos = fonteDados.obterAlunos();
        List<ResultadoEmissaoDTO> relatorio = new ArrayList<>();

        // 1. Resolve a pasta do Google Drive APENAS UMA VEZ antes do loop
        String folderId = driveService.obterOuCriarPastaEvento(requestDTO.nomeEvento());

        log.info("Iniciando geração, envio e backup de {} certificados.", alunos.size());

        for (AlunoCertificado aluno : alunos) {
            try {
                // Gera o PDF
                byte[] pdf = pdfService.gerarCertificado(aluno, requestDTO);

                // Envia por E-mail
                emailService.enviarCertificado(aluno.getEmail(), aluno.getNome(), requestDTO.nomeEvento(), pdf);

                // NOVO: Faz o Backup no Google Drive
                String nomePdf = "Certificado_" + aluno.getNome().replaceAll("\\s+", "_") + ".pdf";
                driveService.fazerUploadCertificado(pdf, nomePdf, folderId);

                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), true, null));

            } catch (Exception e) {
                log.error("Falha ao emitir para o aluno: {}", aluno.getEmail(), e);
                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), false, e.getMessage()));
            }
        }

        return relatorio;
    }

    /**
     * Factory Method interno: Define a estratégia baseada no que veio na requisição.
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