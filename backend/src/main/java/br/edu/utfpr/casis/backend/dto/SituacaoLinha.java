package br.edu.utfpr.casis.backend.dto;

/**
 * Resultado da validação de uma linha do CSV na pré-visualização do lote.
 */
public enum SituacaoLinha {
    /**
     * A linha não tem problemas e será emitida.
     */
    VALIDA,

    /**
     * A linha será emitida, mas tem algo que a diretoria deve conferir (ex.: RA ausente, homônimo).
     */
    AVISO,

    /**
     * A linha não será emitida (ex.: nome vazio, e-mail inválido ou repetido).
     */
    ERRO
}
