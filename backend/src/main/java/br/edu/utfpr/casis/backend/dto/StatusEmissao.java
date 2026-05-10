package br.edu.utfpr.casis.backend.dto;

/**
 * Enumeração que representa os possíveis status após a tentativa de emissão
 * de um certificado para um participante.
 */
public enum StatusEmissao {
    /**
     * O certificado foi gerado e enviado com sucesso.
     */
    SUCESSO,

    /**
     * Ocorreu um erro durante a geração ou envio do certificado.
     */
    ERRO,

    /**
     * Um certificado para o participante no evento já existe.
     * O sistema abortou a geração para evitar duplicatas.
     */
    EXISTENTE
}