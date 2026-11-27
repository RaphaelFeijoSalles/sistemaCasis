package br.edu.utfpr.casis.backend.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formata o período de realização de um evento por extenso, para o texto do certificado (F3).
 *
 * <p>Exemplos:</p>
 * <ul>
 *   <li>um dia: "em 15 de março de 2026";</li>
 *   <li>mesmo mês: "no período de 01 a 05 de abril de 2026";</li>
 *   <li>meses diferentes no mesmo ano: "no período de 28 de abril a 02 de maio de 2026";</li>
 *   <li>anos diferentes: "no período de 28 de dezembro de 2026 a 03 de janeiro de 2027".</li>
 * </ul>
 */
final class PeriodoFormatter {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd", PT_BR);
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd' de 'MMMM", PT_BR);
    private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd' de 'MMMM' de 'yyyy", PT_BR);

    private PeriodoFormatter() {
    }

    /**
     * Monta a frase do período que segue "realizado" no certificado.
     *
     * @param inicio data de início do evento (obrigatória, já validada).
     * @param fim data de fim do evento; {@code null} ou igual ao início indica evento de um dia.
     * @return "em ..." para evento de um dia, ou "no período de ... a ..." na forma mais compacta possível.
     */
    static String formatar(LocalDate inicio, LocalDate fim) {
        if (fim == null || fim.equals(inicio)) {
            return "em " + inicio.format(DIA_MES_ANO);
        }

        String textoInicio;
        if (inicio.getYear() != fim.getYear()) {
            textoInicio = inicio.format(DIA_MES_ANO);
        } else if (inicio.getMonth() != fim.getMonth()) {
            textoInicio = inicio.format(DIA_MES);
        } else {
            textoInicio = inicio.format(DIA);
        }

        return "no período de " + textoInicio + " a " + fim.format(DIA_MES_ANO);
    }
}
