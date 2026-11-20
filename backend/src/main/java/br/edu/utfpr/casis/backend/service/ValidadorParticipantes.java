package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.LinhaValidadaDTO;
import br.edu.utfpr.casis.backend.dto.SituacaoLinha;

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

    private static LinhaValidadaDTO montar(LinhaCsv linha, SituacaoLinha situacao, List<String> mensagens) {
        return new LinhaValidadaDTO(linha.linha(), linha.nome(), linha.email(), linha.ra(), situacao, List.copyOf(mensagens));
    }
}
