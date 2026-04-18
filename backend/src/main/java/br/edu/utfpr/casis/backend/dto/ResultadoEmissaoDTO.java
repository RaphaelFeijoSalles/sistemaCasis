package br.edu.utfpr.casis.backend.dto;

/**
 * Representa o resultado final do disparo para um único aluno.
 */
public record ResultadoEmissaoDTO(
        String nome,
        String email,
        StatusEmissao status,
        String mensagemErro
) {}