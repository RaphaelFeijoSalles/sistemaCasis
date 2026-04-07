package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.service.strategy.FonteDadosCertificadoStrategy;
import br.edu.utfpr.casis.backend.service.strategy.FonteDadosCsvStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmissaoService {

    private final CsvService csvService;
    private final PdfService pdfService;
    //private final EmailService emailService; serviço que vai mandar o Email

    /**
     * Orquestra o fluxo de extração, geração e envio.
     */
    public void processarEmissao(EmissaoLoteEventoRequestDTO requestDTO) {

        // 1. Descobre de onde vêm os dados (Aplica o Padrão Strategy internamente)
        FonteDadosCertificadoStrategy fonteDados = definirEstrategia(requestDTO);
        List<AlunoCertificado> alunos = fonteDados.obterAlunos();

        log.info("Iniciando geração e envio de {} certificados.", alunos.size());

        // 2. Itera e processa
        for (AlunoCertificado aluno : alunos) {
            try {
                // Alinhamento corrigido: passando o DTO completo para o PdfService
                byte[] pdf = pdfService.gerarCertificado(aluno, requestDTO);

                // Mock do envio de e-mail (só para testar a geração do PDF primeiro)
                log.info("PDF gerado em memória para {}. (Envio de email pendente de implementação)", aluno.getNome());

            } catch (Exception e) {
                // Em lotes, se der erro em 1 aluno, não queremos parar os outros 99.
                log.error("Falha ao emitir para o aluno: {}", aluno.getEmail(), e);
            }
        }
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