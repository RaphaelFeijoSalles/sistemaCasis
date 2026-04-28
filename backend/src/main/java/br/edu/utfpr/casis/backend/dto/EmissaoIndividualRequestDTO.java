package br.edu.utfpr.casis.backend.dto;

import java.time.LocalDate;

public record EmissaoIndividualRequestDTO(
        String nomeEvento,
        LocalDate dataRealizacao,
        Integer cargaHoraria,
        String nomeParticipante,
        String emailParticipante,
        String raParticipante
) {}