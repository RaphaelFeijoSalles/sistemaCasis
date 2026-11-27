package br.edu.utfpr.casis.backend.service;

/**
 * Linha bruta de participante lida do CSV, antes de qualquer validação.
 *
 * @param linha número da linha no arquivo (o cabeçalho é a linha 1).
 * @param nome nome do participante, sem espaços nas bordas; vazio se a célula faltar.
 * @param email e-mail do participante, sem espaços nas bordas; vazio se a célula faltar.
 * @param ra Registro Acadêmico, sem espaços nas bordas; {@code null} se não houver coluna ou célula de RA.
 * @param horas texto bruto da coluna {@code Horas}, sem espaços nas bordas; {@code null} se não houver coluna ou célula de horas.
 * @param curso texto bruto da coluna {@code Curso} (F4), sem espaços nas bordas; {@code null} se não houver coluna ou célula de curso.
 */
public record LinhaCsv(
        int linha,
        String nome,
        String email,
        String ra,
        String horas,
        String curso
) {

    /**
     * Linha sem a coluna {@code Curso}, como nos CSVs anteriores à F4.
     *
     * @param linha número da linha no arquivo.
     * @param nome nome do participante.
     * @param email e-mail do participante.
     * @param ra Registro Acadêmico, ou {@code null}.
     * @param horas texto bruto da coluna {@code Horas}, ou {@code null}.
     */
    public LinhaCsv(int linha, String nome, String email, String ra, String horas) {
        this(linha, nome, email, ra, horas, null);
    }
}
