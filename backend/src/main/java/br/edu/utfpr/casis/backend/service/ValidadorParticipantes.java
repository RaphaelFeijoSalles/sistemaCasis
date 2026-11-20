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

    private ValidadorParticipantes() {
    }

    /**
     * Valida as linhas do CSV na ordem do arquivo.
     * <p>
     * Erros (a linha não é emitida): nome vazio, e-mail vazio ou fora do padrão, e-mail repetido.
     * Avisos (a linha é emitida): homônimo que gera o mesmo nome de PDF, RA não informado.
     * Repetição e homônimo só são comparados com linhas anteriores que serão emitidas.
     *
     * @param linhas linhas brutas lidas do CSV.
     * @return uma {@link LinhaValidadaDTO} por linha, na mesma ordem.
     */
    static List<LinhaValidadaDTO> validarLinhas(List<LinhaCsv> linhas) {
        Map<String, Integer> linhaPorEmail = new HashMap<>();
        Map<String, Integer> linhaPorArquivo = new HashMap<>();
        List<LinhaValidadaDTO> resultado = new ArrayList<>();

        for (LinhaCsv linha : linhas) {
            List<String> erros = new ArrayList<>();

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

            if (!erros.isEmpty()) {
                resultado.add(montar(linha, SituacaoLinha.ERRO, erros));
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
            resultado.add(montar(linha, avisos.isEmpty() ? SituacaoLinha.VALIDA : SituacaoLinha.AVISO, avisos));
        }

        return resultado;
    }

    /**
     * Valida os dados do evento informados no formulário.
     * Interrompe no primeiro problema encontrado.
     *
     * @param nomeEvento nome oficial do evento.
     * @param dataRealizacao data em que o evento ocorreu.
     * @param cargaHoraria carga horária em horas decimais.
     * @throws DadosInvalidosException se algum dado estiver ausente ou inválido.
     */
    static void validarEvento(String nomeEvento, LocalDate dataRealizacao, Double cargaHoraria) {
        if (nomeEvento == null || nomeEvento.isBlank()) {
            throw new DadosInvalidosException("O nome do evento é obrigatório.");
        }
        if (dataRealizacao == null) {
            throw new DadosInvalidosException("A data de realização é obrigatória.");
        }
        if (cargaHoraria == null || !Double.isFinite(cargaHoraria) || cargaHoraria <= 0) {
            throw new DadosInvalidosException("A carga horária deve ser um número maior que zero.");
        }
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

    private static LinhaValidadaDTO montar(LinhaCsv linha, SituacaoLinha situacao, List<String> mensagens) {
        return new LinhaValidadaDTO(linha.linha(), linha.nome(), linha.email(), linha.ra(), situacao, List.copyOf(mensagens));
    }
}
