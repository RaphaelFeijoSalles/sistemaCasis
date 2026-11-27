package br.edu.utfpr.casis.backend.service;

/**
 * Regras do RA (registro acadêmico) do participante.
 * <p>
 * Desde a F8 o RA é obrigatório. Quem não tem RA (palestrante, aluno de outra
 * instituição) é marcado com o valor {@link #EXTERNO} no próprio campo do RA,
 * sem diferenciar maiúsculas de minúsculas e ignorando os espaços das bordas.
 * O RA não tem validação de formato: qualquer texto não vazio vale.
 */
final class RegistroAcademico {

    /** Marcador de participante externo, sem RA. */
    static final String EXTERNO = "EXTERNO";

    private RegistroAcademico() {
    }

    /**
     * Indica se o RA informado é o marcador de participante externo.
     *
     * @param ra RA como veio da planilha ou do formulário; pode ser {@code null}.
     * @return {@code true} se o valor, sem os espaços das bordas, for igual a
     *         {@link #EXTERNO} ignorando maiúsculas e minúsculas; {@code false}
     *         para {@code null}, vazio ou qualquer outro texto.
     */
    static boolean ehExterno(String ra) {
        return ra != null && EXTERNO.equalsIgnoreCase(ra.trim());
    }

    /**
     * Normaliza o RA: remove os espaços das bordas e troca qualquer grafia do
     * marcador externo (ex.: {@code " externo "}) por {@link #EXTERNO}.
     * <p>
     * Não converte RA em branco para {@code null}: devolve o texto sem bordas,
     * que pode ser {@code ""}. Decidir se o RA vazio é erro cabe ao validador.
     *
     * @param ra RA como veio da planilha ou do formulário; pode ser {@code null}.
     * @return {@code null} se a entrada for {@code null}; {@link #EXTERNO} se for
     *         o marcador externo; caso contrário, o RA sem os espaços das bordas.
     */
    static String normalizar(String ra) {
        if (ra == null) {
            return null;
        }
        return ehExterno(ra) ? EXTERNO : ra.trim();
    }
}
