package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.LinhaValidadaDTO;
import br.edu.utfpr.casis.backend.dto.SituacaoLinha;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Regras puras de validação dos participantes de um lote, sem efeitos colaterais.
 * Usada tanto na pré-visualização quanto na emissão.
 */
final class ValidadorParticipantes {

    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TAMANHO_MAXIMO_VALOR_NA_MENSAGEM = 30;

    /** Curso usado quando a requisição não informa curso (frontend anterior à F4), como era antes. */
    static final String CURSO_LEGADO = "SI";

    private ValidadorParticipantes() {
    }

    /**
     * Valida as linhas do CSV na ordem do arquivo.
     * <p>
     * Erros (a linha não é emitida): nome vazio, e-mail vazio ou fora do padrão, e-mail repetido, horas inválidas.
     * Avisos (a linha é emitida): homônimo que gera o mesmo nome de PDF, RA não informado.
     * Repetição e homônimo só são comparados com linhas anteriores que serão emitidas.
     * <p>
     * Horas (F2): a coluna {@code Horas} preenchida prevalece; célula vazia ou coluna ausente usa a carga do formulário.
     * <p>
     * Curso (F4): a coluna {@code Curso} preenchida prevalece e precisa ser um apelido configurado
     * (senão a linha é erro); célula vazia ou coluna ausente usa o curso padrão do formulário.
     *
     * @param linhas linhas brutas lidas do CSV.
     * @param cargaPadrao carga horária do formulário, em horas decimais, usada quando a linha não traz horas.
     * @param cursoPadrao código do curso padrão do formulário (ex.: "SI"), já validado, usado quando a linha não traz curso.
     * @param interpretadorCurso cursos configurados da gestão.
     * @return uma {@link LinhaValidadaDTO} por linha, na mesma ordem.
     */
    static List<LinhaValidadaDTO> validarLinhas(List<LinhaCsv> linhas, Double cargaPadrao,
                                                String cursoPadrao, InterpretadorCurso interpretadorCurso) {
        return validar(linhas, cargaPadrao, cursoPadrao, interpretadorCurso);
    }

    private static List<LinhaValidadaDTO> validar(List<LinhaCsv> linhas, Double cargaPadrao,
                                                  String cursoPadrao, InterpretadorCurso interpretadorCurso) {
        Map<String, Integer> linhaPorEmail = new HashMap<>();
        Map<String, Integer> linhaPorArquivo = new HashMap<>();
        List<LinhaValidadaDTO> resultado = new ArrayList<>();

        for (LinhaCsv linha : linhas) {
            List<String> erros = new ArrayList<>();
            boolean horasDoFormulario = linha.horas() == null || linha.horas().isBlank();
            Double horas = horasDoFormulario
                    ? cargaPadrao
                    : InterpretadorHoras.interpretar(linha.horas()).orElse(null);

            boolean cursoDoFormulario = linha.curso() == null || linha.curso().isBlank();
            String curso = cursoDoFormulario
                    ? cursoPadrao
                    : interpretadorCurso.interpretar(linha.curso()).orElse(null);
            String cursoTexto = curso == null ? null
                    : interpretadorCurso.curso(curso).map(c -> c.nomeCompleto()).orElse(null);

            if (linha.nome().isBlank()) {
                erros.add("Nome não informado");
            }

            String chaveEmail = linha.email().trim().toLowerCase(Locale.ROOT);
            if (linha.email().isBlank()) {
                erros.add("E-mail não informado");
            } else if (!PADRAO_EMAIL.matcher(linha.email().trim()).matches()) {
                erros.add("E-mail inválido: " + linha.email());
            } else if (linhaPorEmail.containsKey(chaveEmail)) {
                erros.add("E-mail repetido (linha " + linhaPorEmail.get(chaveEmail) + ")");
            }

            if (horas == null) {
                erros.add("Horas inválidas: " + resumir(linha.horas()));
            }

            if (curso == null) {
                erros.add("Curso inválido: " + resumir(linha.curso()));
            }

            Curso dadosCurso = new Curso(curso, cursoTexto, cursoDoFormulario);

            if (!erros.isEmpty()) {
                resultado.add(montar(linha, horas, horasDoFormulario, dadosCurso, SituacaoLinha.ERRO, erros));
                continue;
            }

            List<String> avisos = new ArrayList<>();
            String nomeArquivo = NomeArquivoCertificado.gerar(linha.nome());
            if (linhaPorArquivo.containsKey(nomeArquivo)) {
                avisos.add("Mesmo nome da linha " + linhaPorArquivo.get(nomeArquivo) + ": o Drive terá dois arquivos com o mesmo nome");
            }
            if (linha.ra() == null || linha.ra().isBlank()) {
                avisos.add("RA não informado");
            }

            linhaPorEmail.put(chaveEmail, linha.linha());
            linhaPorArquivo.putIfAbsent(nomeArquivo, linha.linha());
            resultado.add(montar(linha, horas, horasDoFormulario, dadosCurso, avisos.isEmpty() ? SituacaoLinha.VALIDA : SituacaoLinha.AVISO, avisos));
        }

        return resultado;
    }

    /** Duração máxima de um evento (F3): data de fim até 366 dias depois do início. */
    static final int MAXIMO_DIAS_EVENTO = 366;

    /**
     * Valida os dados do evento informados no formulário.
     * Interrompe no primeiro problema encontrado.
     *
     * @param nomeEvento nome oficial do evento.
     * @param dataRealizacao data de início do evento.
     * @param dataFim data de fim do evento (F3), opcional; se informada, não pode ser anterior ao início
     *                nem passar de {@link #MAXIMO_DIAS_EVENTO} dias depois dele.
     * @param cargaHoraria carga horária em horas decimais, entre 1 minuto e {@link InterpretadorHoras#MAXIMO_HORAS} horas.
     * @throws DadosInvalidosException se algum dado estiver ausente ou inválido.
     */
    static void validarEvento(String nomeEvento, LocalDate dataRealizacao, LocalDate dataFim, Double cargaHoraria) {
        if (nomeEvento == null || nomeEvento.isBlank()) {
            throw new DadosInvalidosException("O nome do evento é obrigatório.");
        }
        if (dataRealizacao == null) {
            throw new DadosInvalidosException("A data de realização é obrigatória.");
        }
        if (dataFim != null && dataFim.isBefore(dataRealizacao)) {
            throw new DadosInvalidosException("A data de fim não pode ser anterior à data de início.");
        }
        if (dataFim != null && dataFim.isAfter(dataRealizacao.plusDays(MAXIMO_DIAS_EVENTO))) {
            throw new DadosInvalidosException("A data de fim deve ser de no máximo " + MAXIMO_DIAS_EVENTO + " dias depois da data de início.");
        }
        if (cargaHoraria != null && Double.isFinite(cargaHoraria) && cargaHoraria > InterpretadorHoras.MAXIMO_HORAS) {
            throw new DadosInvalidosException("A carga horária deve ser de no máximo 1.000 horas.");
        }
        if (!InterpretadorHoras.dentroDosLimites(cargaHoraria)) {
            throw new DadosInvalidosException("A carga horária deve ser um número maior que zero.");
        }
    }

    /**
     * Resolve o curso padrão do formulário (lote) ou o curso do participante (unitário).
     * Ausente ou em branco vale {@link #CURSO_LEGADO}, para o frontend anterior à F4.
     *
     * @param valor código ou apelido informado na requisição, ou {@code null}.
     * @param interpretadorCurso cursos configurados da gestão.
     * @return o código do curso (ex.: "SI").
     * @throws DadosInvalidosException se o valor não for um curso configurado.
     */
    static String resolverCurso(String valor, InterpretadorCurso interpretadorCurso) {
        if (valor == null || valor.isBlank()) {
            return CURSO_LEGADO;
        }
        return interpretadorCurso.interpretar(valor)
                .orElseThrow(() -> new DadosInvalidosException("Curso inválido: " + resumir(valor.trim())));
    }

    /**
     * Valida o participante da emissão unitária.
     * Interrompe no primeiro problema encontrado.
     *
     * @param nome nome do participante.
     * @param email e-mail do participante.
     * @throws DadosInvalidosException se o nome estiver vazio ou o e-mail ausente ou fora do padrão.
     */
    static void validarParticipante(String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do participante é obrigatório.");
        }
        if (email == null || email.isBlank()) {
            throw new DadosInvalidosException("O e-mail do participante é obrigatório.");
        }
        if (!PADRAO_EMAIL.matcher(email.trim()).matches()) {
            throw new DadosInvalidosException("E-mail do participante inválido: " + email.trim());
        }
    }

    /**
     * Corta valores muito longos para a mensagem não ocupar a prévia inteira (ex.: uma célula com milhares de dígitos).
     */
    private static String resumir(String valor) {
        return valor.length() > TAMANHO_MAXIMO_VALOR_NA_MENSAGEM
                ? valor.substring(0, TAMANHO_MAXIMO_VALOR_NA_MENSAGEM) + "…"
                : valor;
    }

    /** Curso resolvido de uma linha: código, nome completo e se veio do formulário. */
    private record Curso(String codigo, String texto, boolean doFormulario) {
    }

    private static LinhaValidadaDTO montar(LinhaCsv linha, Double horas, boolean horasDoFormulario, Curso curso,
                                           SituacaoLinha situacao, List<String> mensagens) {
        String horasTexto = horas == null ? null : CargaHorariaFormatter.formatarTexto(horas);
        return new LinhaValidadaDTO(linha.linha(), linha.nome(), linha.email(), linha.ra(),
                horas, horasTexto, horasDoFormulario, curso.codigo(), curso.texto(), curso.doFormulario(),
                situacao, List.copyOf(mensagens));
    }
}
