package br.edu.utfpr.casis.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Classe base abstrata para qualquer pessoa que receberá um certificado.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AlunoCertificado {
    private String nome;
    private String email;
    private String documento; // Pode ser RA (para eventos) ou CPF (para gestão)
}