package br.edu.utfpr.casis.backend.dto;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para requisições de emissão de certificados em lote.
 * Contém os dados do evento e o arquivo CSV com a lista de participantes.
 *
 * @param nomeEvento Nome do evento ou curso.
 * @param dataRealizacao Data em que o evento ocorreu.
 * @param cargaHoraria Carga horária total do evento.
 * @param arquivoCsv Arquivo CSV contendo os dados dos participantes.
 */
public record EmissaoLoteEventoRequestDTO(
        String nomeEvento,
        LocalDate dataRealizacao,
        Double cargaHoraria,
        MultipartFile arquivoCsv
) {}