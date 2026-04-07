package br.edu.utfpr.casis.backend.dto;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

/**
 * Record DTO imutável para recebimento de requisições de eventos via CSV.
 */
public record EmissaoLoteEventoRequestDTO(
        String nomeEvento,
        LocalDate dataRealizacao,
        Integer cargaHoraria,
        MultipartFile arquivoCsv
) {}