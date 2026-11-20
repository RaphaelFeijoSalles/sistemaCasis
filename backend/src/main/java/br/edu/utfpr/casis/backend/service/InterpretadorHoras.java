package br.edu.utfpr.casis.backend.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regra pura para interpretar o texto da coluna {@code Horas} do CSV, sem efeitos colaterais.
 * <p>
 * Formatos aceitos:
 * <ul>
 *     <li>decimal, com ponto ou vírgula: {@code 2}, {@code 1.5}, {@code 1,5};</li>
 *     <li>duração {@code HH:MM} ou {@code HH:MM:SS}: {@code 01:30}, {@code 1:30:00}. Os segundos são ignorados.</li>
 * </ul>
 * O resultado precisa ser maior que zero.
 */
final class InterpretadorHoras {

    private static final Pattern PADRAO_DECIMAL = Pattern.compile("^\\d+([.,]\\d+)?$");
    private static final Pattern PADRAO_DURACAO = Pattern.compile("^(\\d+):([0-5]\\d)(:[0-5]\\d)?$");

    private InterpretadorHoras() {
    }

    /**
     * Converte o texto de uma célula de horas em horas decimais.
     * Espaços nas bordas são ignorados.
     *
     * @param texto valor da célula, como veio do CSV.
     * @return as horas decimais (ex.: {@code 1.5} para {@code "01:30"}), ou vazio se o texto
     *         for nulo, em branco, fora dos formatos aceitos ou resultar em zero.
     *         Quem chama decide o que fazer com a célula em branco.
     */
    static Optional<Double> interpretar(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }

        String valor = texto.trim();
        Double horas = null;

        if (PADRAO_DECIMAL.matcher(valor).matches()) {
            horas = Double.parseDouble(valor.replace(',', '.'));
        } else {
            Matcher duracao = PADRAO_DURACAO.matcher(valor);
            if (duracao.matches()) {
                horas = Double.parseDouble(duracao.group(1)) + Integer.parseInt(duracao.group(2)) / 60.0;
            }
        }

        if (horas == null || !Double.isFinite(horas) || horas <= 0) {
            return Optional.empty();
        }
        return Optional.of(horas);
    }
}
