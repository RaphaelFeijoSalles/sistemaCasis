package br.edu.utfpr.casis.backend.dto;

import java.util.List;

/**
 * DTO (Data Transfer Object) com a pré-visualização de um lote, antes de qualquer emissão.
 *
 * @param totalLinhas Quantidade de linhas de participantes (linhas em branco não contam).
 * @param validas Quantidade de linhas sem problemas.
 * @param comAviso Quantidade de linhas que serão emitidas, mas têm aviso.
 * @param comErro Quantidade de linhas que não serão emitidas.
 * @param linhas Resultado da validação de cada linha, na ordem do arquivo.
 */
public record PreVisualizacaoLoteDTO(
        int totalLinhas,
        int validas,
        int comAviso,
        int comErro,
        List<LinhaValidadaDTO> linhas
) {}
