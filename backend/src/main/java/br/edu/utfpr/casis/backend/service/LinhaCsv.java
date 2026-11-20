package br.edu.utfpr.casis.backend.service;

/**
 * Linha bruta de participante lida do CSV, antes de qualquer validação.
 *
 * @param linha número da linha no arquivo (o cabeçalho é a linha 1).
 * @param nome nome do participante, sem espaços nas bordas; vazio se a célula faltar.
 * @param email e-mail do participante, sem espaços nas bordas; vazio se a célula faltar.
 * @param ra Registro Acadêmico, sem espaços nas bordas; {@code null} se não houver coluna ou célula de RA.
 * @param horas texto bruto da coluna {@code Horas}, sem espaços nas bordas; {@code null} se não houver coluna ou célula de horas.
 */
public record LinhaCsv(
        int linha,
        String nome,
        String email,
        String ra,
        String horas
) {}
