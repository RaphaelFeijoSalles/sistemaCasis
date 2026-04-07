package br.edu.utfpr.casis.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa os dados extraídos de uma linha do arquivo CSV.
 * O CSV é gerado a partir da lista de presença do Google Forms.
 * * Utilizamos Lombok (@Data) para gerar automaticamente os métodos
 * Getters, Setters, equals, hashCode e toString em tempo de compilação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlunoCsv {

    /**
     * Nome completo do aluno que sairá estampado no certificado.
     */
    private String nome;

    /**
     * E-mail institucional ou pessoal para o disparo do arquivo PDF.
     */
    private String email;

    /**
     * Registro Acadêmico (RA) ou CPF do aluno.
     * Necessário principalmente para os certificados de Gestão.
     */
    private String documento;
}