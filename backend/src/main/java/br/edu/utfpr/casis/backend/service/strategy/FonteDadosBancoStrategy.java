package br.edu.utfpr.casis.backend.service.strategy;


import br.edu.utfpr.casis.backend.model.AlunoCertificado;

import java.util.List;

/**
 * Estratégia de fonte de dados que obterá os participantes a partir do Banco de Dados.
 * Esta classe é um preparo para a Fase 2 do sistema (gestão de membros e eventos via DB).
 */
public class FonteDadosBancoStrategy implements FonteDadosCertificadoStrategy {

    // Futuramente injetaremos o Repositório JPA aqui
    // private final MembroRepository membroRepository;

    /**
     * Obtém a lista de alunos a serem certificados consultando o banco de dados.
     * Na Fase atual (1), retorna uma lista vazia como placeholder.
     *
     * @return Uma lista de {@link AlunoCertificado}.
     */
    @Override
    public List<AlunoCertificado> obterAlunos() {
        // Futuro: membroRepository.findMembrosComContratoFinalizado();
        // E mapeia para AlunoCertificado
        return List.of();
    }
}