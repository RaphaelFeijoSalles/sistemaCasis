package br.edu.utfpr.casis.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Representa um participante de um evento que receberá um certificado.
 * Estende a classe {@link AlunoCertificado} com informações específicas de eventos.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlunoCertificadoEvento extends AlunoCertificado {
    /**
     * Tipo de participação do aluno no evento.
     * Exemplos: OUVINTE, PALESTRANTE, ORGANIZADOR.
     */
    private TipoParticipacao tipoParticipacao;
}