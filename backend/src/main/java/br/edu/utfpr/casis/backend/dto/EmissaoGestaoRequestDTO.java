package br.edu.utfpr.casis.backend.dto;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para requisições de emissão de certificados de gestão.
 * Este DTO é projetado para um futuro onde os dados dos membros da gestão serão
 * obtidos a partir de um banco de dados, eliminando a necessidade de envio de arquivos.
 *
 * @param idDiretoria ID da diretoria para a qual os certificados serão emitidos.
 *                    Pode ser usado para buscar os membros de uma gestão específica.
 * @param nomeGestao Nome da gestão, por exemplo, "Gestão 2025/2026".
 * @param dataEmissaoCustomizada Data de emissão a ser utilizada no certificado.
 *                               Permite emissão com datas retroativas.
 */
public record EmissaoGestaoRequestDTO(
        Long idDiretoria,
        String nomeGestao,
        LocalDate dataEmissaoCustomizada
) {}