package br.edu.utfpr.casis.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Representa um participante isolado de um evento do CA.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlunoCertificadoEvento extends AlunoCertificado {
    // Para eventos, podemos adicionar o tipo de participação no futuro
    private TipoParticipacao tipoParticipacao; // ex: "Ouvinte", "Palestrante", "Organizador"
}