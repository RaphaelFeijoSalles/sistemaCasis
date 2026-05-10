package br.edu.utfpr.casis.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Classe base abstrata que representa um participante a ser certificado.
 * Define os atributos comuns a todos os tipos de certificados.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AlunoCertificado {
    /**
     * Nome completo do participante.
     */
    private String nome;

    /**
     * E-mail do participante.
     */
    private String email;

    /**
     * Registro Acadêmico (RA) do participante.
     */
    private String ra;
}