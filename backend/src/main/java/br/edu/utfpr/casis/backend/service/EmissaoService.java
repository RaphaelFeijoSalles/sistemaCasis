package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoIndividualRequestDTO;
import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.dto.LinhaValidadaDTO;
import br.edu.utfpr.casis.backend.dto.PreVisualizacaoLoteDTO;
import br.edu.utfpr.casis.backend.dto.ResultadoEmissaoDTO;
import br.edu.utfpr.casis.backend.dto.SituacaoLinha;
import br.edu.utfpr.casis.backend.dto.StatusEmissao;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.model.AlunoCertificadoEvento;
import br.edu.utfpr.casis.backend.model.TipoParticipacao;
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
    private final InterpretadorCurso interpretadorCurso;

    /**
     * Valida o lote e devolve a pré-visualização, sem nenhum efeito colateral:
     * não gera PDF, não envia e-mail e não acessa Drive nem Sheets.
     *
     * @param requestDTO DTO contendo os dados do evento e o arquivo CSV.
     * @return O resumo e o resultado da validação de cada linha do CSV.
     * @throws DadosInvalidosException Se os dados do evento ou o CSV forem inválidos.
     */
    public PreVisualizacaoLoteDTO preVisualizarLote(EmissaoLoteEventoRequestDTO requestDTO) {
        List<LinhaValidadaDTO> linhas = validarLote(requestDTO);

        int validas = contar(linhas, SituacaoLinha.VALIDA);
        int comAviso = contar(linhas, SituacaoLinha.AVISO);
        int comErro = contar(linhas, SituacaoLinha.ERRO);
        log.info("Pré-visualização do evento '{}': {} linhas ({} válidas, {} com aviso, {} com erro).",
                requestDTO.nomeEvento(), linhas.size(), validas, comAviso, comErro);

        return new PreVisualizacaoLoteDTO(linhas.size(), validas, comAviso, comErro, linhas);
    }

    /**
     * Valida os dados do evento e as linhas do CSV, sem efeitos colaterais.
     * Usado pela pré-visualização e pela emissão, para que as duas apliquem as mesmas regras.
     */
    private List<LinhaValidadaDTO> validarLote(EmissaoLoteEventoRequestDTO requestDTO) {
        ValidadorParticipantes.validarEvento(requestDTO.nomeEvento(), requestDTO.dataRealizacao(), requestDTO.cargaHoraria());
        if (requestDTO.arquivoCsv() == null || requestDTO.arquivoCsv().isEmpty()) {
            throw new DadosInvalidosException("O arquivo CSV é obrigatório.");
        }
        String cursoPadrao = ValidadorParticipantes.resolverCurso(requestDTO.cursoPadrao(), interpretadorCurso);
        return ValidadorParticipantes.validarLinhas(csvService.lerLinhas(requestDTO.arquivoCsv()),
                requestDTO.cargaHoraria(), cursoPadrao, interpretadorCurso);
    }

    private ResultadoEmissaoDTO resultadoErroValidacao(LinhaValidadaDTO linha) {
        return new ResultadoEmissaoDTO(linha.nome(), linha.email(), StatusEmissao.ERRO, String.join("; ", linha.mensagens()));
    }

    private int contar(List<LinhaValidadaDTO> linhas, SituacaoLinha situacao) {
        return (int) linhas.stream().filter(linha -> linha.situacao() == situacao).count();
    }

    /**
     * Orquestra o fluxo de emissão de certificados em lote para um evento.
     *
     * @param requestDTO DTO contendo os dados do evento e a fonte de dados (CSV).
     * @return Uma lista de resultados (relatório) contendo o status da emissão para cada participante.
     */
    public List<ResultadoEmissaoDTO> processarEmissao(EmissaoLoteEventoRequestDTO requestDTO) {

        // 0. Valida tudo antes de qualquer efeito (F1). Lança DadosInvalidosException se o evento ou o CSV forem inválidos.
        List<LinhaValidadaDTO> linhas = validarLote(requestDTO);
        List<ResultadoEmissaoDTO> relatorio = new ArrayList<>();
        int comErro = contar(linhas, SituacaoLinha.ERRO);

        if (comErro == linhas.size()) {
            log.warn("Nenhuma linha emitível no lote do evento '{}'. Drive, PDF, e-mail e Sheets não foram acionados.", requestDTO.nomeEvento());
            linhas.forEach(linha -> relatorio.add(resultadoErroValidacao(linha)));
            return relatorio;
        }

        // 1. Resolve a pasta do Google Drive com o Nome e a Data para padronizar Ano/Semestre
        String folderId = driveService.obterOuCriarPastaEvento(requestDTO.nomeEvento(), requestDTO.dataRealizacao());

        // 2. FAZ A VARREDURA UMA ÚNICA VEZ para saber o que já existe lá dentro
        Set<String> arquivosExistentes = driveService.listarArquivosNaPasta(folderId);

        log.info("Iniciando geração para {} certificados ({} linhas com erro serão puladas). {} arquivos já existem na pasta.",
                linhas.size() - comErro, comErro, arquivosExistentes.size());

        for (LinhaValidadaDTO linha : linhas) {
            // Linha inválida: vai para o relatório sem PDF, e-mail, upload ou Sheets
            if (linha.situacao() == SituacaoLinha.ERRO) {
                relatorio.add(resultadoErroValidacao(linha));
                continue;
            }

            AlunoCertificado aluno = AlunoCertificadoEvento.builder()
                    .nome(linha.nome())
                    .email(linha.email())
                    .ra(linha.ra())
                    .tipoParticipacao(TipoParticipacao.OUVINTE) // Valor padrão para emissões em lote de forms comuns
                    .build();

            // Limpa o nome para padronizar o nome do arquivo
            String nomePdf = NomeArquivoCertificado.gerar(aluno.getNome());

            // 3. VERIFICAÇÃO DE DUPLICIDADE: Se já existe no Drive, pula o processo inteiro!
            if (arquivosExistentes.contains(nomePdf)) {
                relatorio.add(new ResultadoEmissaoDTO(aluno.getNome(), aluno.getEmail(), StatusEmissao.EXISTENTE, "Certificado já consta no Drive."));
                continue;
            }

            // Horas da própria linha (F2): coluna Horas do CSV ou, se vazia, a carga do formulário
            EmissaoLoteEventoRequestDTO dadosDaLinha = new EmissaoLoteEventoRequestDTO(
                    requestDTO.nomeEvento(), requestDTO.dataRealizacao(), linha.horas(), linha.curso(), requestDTO.arquivoCsv());

            try {
                // Gera, Envia e faz Upload apenas se for novo
                byte[] pdf = pdfService.gerarCertificado(aluno, dadosDaLinha);
                emailService.enviarCertificado(aluno.getEmail(), aluno.getNome(), requestDTO.nomeEvento(), pdf);
                driveService.fazerUploadCertificado(pdf, nomePdf, folderId);

                // Registra na planilha de controle
                sheetsService.registrarEmissao(aluno.getNome(), aluno.getRa(), null, requestDTO.dataRealizacao(), requestDTO.nomeEvento(), linha.horas());

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
        // 0. Valida antes de qualquer efeito (F1). Lança DadosInvalidosException se algo estiver inválido.
        ValidadorParticipantes.validarEvento(dto.nomeEvento(), dto.dataRealizacao(), dto.cargaHoraria());
        ValidadorParticipantes.validarParticipante(dto.nomeParticipante(), dto.emailParticipante());
        String curso = ValidadorParticipantes.resolverCurso(dto.cursoParticipante(), interpretadorCurso);

        // 1. Cria o aluno manualmente
        AlunoCertificado aluno = br.edu.utfpr.casis.backend.model.AlunoCertificadoEvento.builder()
                .nome(dto.nomeParticipante().trim())
                .email(dto.emailParticipante().trim())
                .ra(dto.raParticipante())
                .build();

        // 2. Cria um DTO de lote "falso" (sem arquivo) só para o PdfService aceitar os dados do evento
        EmissaoLoteEventoRequestDTO loteDto = new EmissaoLoteEventoRequestDTO(
                dto.nomeEvento(), dto.dataRealizacao(), dto.cargaHoraria(), curso, null
        );

        // 3. Resolve a pasta e checa se já existe (reaproveitando a lógica)
        String folderId = driveService.obterOuCriarPastaEvento(dto.nomeEvento(), dto.dataRealizacao());
        String nomePdf = NomeArquivoCertificado.gerar(aluno.getNome());

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
     * <p>
     * Sem uso desde a F1: o lote CSV passou a usar {@link CsvService#lerLinhas} e a validação.
     * Mantido como ponto de extensão para a F5 (banco de dados).
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