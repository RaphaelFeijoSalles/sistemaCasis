package br.edu.utfpr.casis.backend.dto;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para requisições de emissão de certificados em lote.
 * Contém os dados do evento e o arquivo CSV com a lista de participantes.
 *
 * @param nomeEvento Nome do evento ou curso.
 * @param dataRealizacao Data de início do evento (para evento de um dia, a única data). Define o
 *                       semestre da pasta no Drive.
 * @param dataFim Data de fim do evento (F3), opcional. Ausente ou igual ao início = evento de um dia.
 * @param cargaHoraria Carga horária total do evento.
 * @param cursoPadrao Curso padrão do formulário (F4), por código ou apelido (ex.: "SI", "CDIA"),
 *                    usado nas linhas sem curso. Ausente = "SI", para o frontend anterior à F4.
 * @param arquivoCsv Arquivo CSV contendo os dados dos participantes.
 */
public record EmissaoLoteEventoRequestDTO(
        String nomeEvento,
        LocalDate dataRealizacao,
        LocalDate dataFim,
        Double cargaHoraria,
        String cursoPadrao,
        MultipartFile arquivoCsv
) {}