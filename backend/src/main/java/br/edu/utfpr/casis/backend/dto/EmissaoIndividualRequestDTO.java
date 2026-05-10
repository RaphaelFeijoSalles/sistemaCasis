package br.edu.utfpr.casis.backend.dto;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para requisições de emissão de certificado individual.
 * Contém os dados do evento e do participante necessários para a geração do certificado.
 *
 * @param nomeEvento Nome do evento ou curso.
 * @param dataRealizacao Data em que o evento ocorreu.
 * @param cargaHoraria Carga horária total do evento.
 * @param nomeParticipante Nome completo do participante.
 * @param emailParticipante E-mail do participante para o qual o certificado será enviado.
 * @param raParticipante Registro Acadêmico (RA) do participante, se aplicável.
 */
public record EmissaoIndividualRequestDTO(
        String nomeEvento,
        LocalDate dataRealizacao,
        Integer cargaHoraria,
        String nomeParticipante,
        String emailParticipante,
        String raParticipante
) {}