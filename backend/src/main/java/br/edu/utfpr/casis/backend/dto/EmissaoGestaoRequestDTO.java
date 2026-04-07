package br.edu.utfpr.casis.backend.dto;

import java.time.LocalDate;

/**
 * Record DTO para disparar a emissão de certificados de conclusão de gestão.
 * Não recebe arquivo, pois os dados virão do Banco de Dados.
 */
public record EmissaoGestaoRequestDTO(
        // Futuramente pode receber um ID de uma diretoria específica, ou ID do aluno
        Long idDiretoria,
        String nomeGestao, // ex: "Gestão 2025/2026"
        LocalDate dataEmissaoCustomizada // Opcional, caso queiram emitir retroativo
) {}