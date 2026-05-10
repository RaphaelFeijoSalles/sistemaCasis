package br.edu.utfpr.casis.backend.dto;

/**
 * DTO (Data Transfer Object) que representa o resultado da emissão de um certificado
 * para um participante específico.
 *
 * @param nome Nome do participante.
 * @param email E-mail do participante.
 * @param status Status final da emissão (SUCESSO, EXISTENTE, FALHA).
 * @param mensagemErro Mensagem de erro em caso de falha, ou nulo se não houver erro.
 */
public record ResultadoEmissaoDTO(
        String nome,
        String email,
        StatusEmissao status,
        String mensagemErro
) {}