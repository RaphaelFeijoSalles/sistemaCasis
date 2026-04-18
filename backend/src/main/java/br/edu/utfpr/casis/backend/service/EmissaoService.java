package br.edu.utfpr.casis.backend.service;

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

                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), StatusEmissao.SUCESSO, null));

            } catch (Exception e) {
                log.error("Falha ao emitir para o aluno: {}", aluno.getEmail(), e);
                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), StatusEmissao.ERRO, e.getMessage()));
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