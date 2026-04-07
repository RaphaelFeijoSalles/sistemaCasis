package br.edu.utfpr.casis.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Representa um membro efetivo do Centro Acadêmico.
 * Essa classe será mapeada como Entidade JPA (@Entity) na Fase 2.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlunoCertificadoGestao extends AlunoCertificado {
    private String cpf;
    private String cargo; // ex: "Primeiro(a) Secretariado(a)"
    private String diretoria; // ex: "Secretariado", "Marketing"
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Integer horasTrabalhadas;
}