package br.edu.utfpr.casis.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Representa um membro da gestão do Centro Acadêmico que receberá um certificado.
 * Esta classe está preparada para ser mapeada como uma Entidade JPA no futuro (Fase 2).
 * Estende a classe {@link AlunoCertificado}.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlunoCertificadoGestao extends AlunoCertificado {

    /**
     * CPF do membro da gestão (frequentemente necessário para certificados oficiais).
     */
    private String cpf;

    /**
     * Cargo ocupado na gestão, por exemplo, "Primeiro(a) Secretariado(a)".
     */
    private String cargo;

    /**
     * Diretoria ou departamento ao qual o membro pertence, por exemplo, "Secretariado".
     */
    private String diretoria;

    /**
     * Data de início das atividades na gestão.
     */
    private LocalDate dataInicio;

    /**
     * Data de término das atividades na gestão.
     */
    private LocalDate dataFim;

    /**
     * Quantidade total de horas trabalhadas durante o período da gestão.
     */
    private Integer horasTrabalhadas;
}