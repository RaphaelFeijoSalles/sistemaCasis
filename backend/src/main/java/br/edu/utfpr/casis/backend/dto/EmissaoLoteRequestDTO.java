package br.edu.utfpr.casis.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) responsável por encapsular os dados
 * recebidos do frontend na requisição de emissão em lote.
 * * Como a requisição contém upload de arquivo, ela deve ser mapeada
 * como multipart/form-data no Controller.
 */
@Data
public class EmissaoLoteRequestDTO {

    /**
     * Nome oficial do evento (ex: "Semana da Computação - Palestra IA").
     */
    private String nomeEvento;

    /**
     * Data em que o evento ocorreu.
     * Pode ser recebida como String e formatada no backend, ou recebida diretamente.
     */
    private LocalDate dataRealizacao;

    /**
     * Carga horária validada pelo CASIS para este evento.
     */
    private Integer cargaHoraria;

    /**
     * Arquivo CSV contendo as colunas de Nome, Email e Documento.
     * A interface MultipartFile do Spring gerencia o arquivo temporário em memória.
     */
    private MultipartFile arquivoCsv;

    /**
     * Define o layout a ser usado: "EVENTO" ou "GESTAO".
     */
    private String tipoCertificado;
}