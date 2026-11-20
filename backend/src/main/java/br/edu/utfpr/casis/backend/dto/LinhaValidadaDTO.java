package br.edu.utfpr.casis.backend.dto;

import java.util.List;

/**
 * DTO (Data Transfer Object) com o resultado da validação de uma linha do CSV.
 *
 * @param linha Número da linha no arquivo (o cabeçalho é a linha 1).
 * @param nome Nome do participante como veio no CSV.
 * @param email E-mail do participante como veio no CSV.
 * @param ra Registro Acadêmico como veio no CSV, ou nulo se ausente.
 * @param horas Horas decimais que irão no certificado, ou nulo se o valor da coluna Horas for inválido.
 * @param horasTexto Horas como aparecem no certificado (ex.: "1 hora e 30 minutos"), ou nulo se inválidas.
 * @param horasDoFormulario Verdadeiro quando as horas vieram da carga horária do formulário (coluna ausente ou célula vazia).
 * @param situacao Situação da linha: VALIDA, AVISO ou ERRO.
 * @param mensagens Motivos do erro ou do aviso; vazia quando a linha é válida.
 */
public record LinhaValidadaDTO(
        int linha,
        String nome,
        String email,
        String ra,
        Double horas,
        String horasTexto,
        boolean horasDoFormulario,
        SituacaoLinha situacao,
        List<String> mensagens
) {}
