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
 * O resultado precisa estar dentro dos limites de {@link #dentroDosLimites(Double)}.
 */
final class InterpretadorHoras {

    /** Maior carga horária aceita, em horas. Também evita estouro no arredondamento do {@link CargaHorariaFormatter}. */
    static final double MAXIMO_HORAS = 1000;

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
     *         for nulo, em branco, fora dos formatos aceitos ou fora dos limites.
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

        return dentroDosLimites(horas) ? Optional.of(horas) : Optional.empty();
    }

    /**
     * Indica se a carga horária pode ir para o certificado: pelo menos 1 minuto depois do
     * arredondamento feito pelo {@link CargaHorariaFormatter} e no máximo {@link #MAXIMO_HORAS}.
     *
     * @param horas carga horária em horas decimais.
     * @return {@code true} se estiver dentro dos limites; {@code false} se for nula, não numérica ou fora deles.
     */
    static boolean dentroDosLimites(Double horas) {
        return horas != null
                && Double.isFinite(horas)
                && horas <= MAXIMO_HORAS
                && Math.round(horas * 60) >= 1;
    }
}
