package br.edu.utfpr.casis.backend.service.strategy;


import br.edu.utfpr.casis.backend.model.AlunoCertificado;

import java.util.List;

/**
 * Interface comum para qualquer fonte de dados de certificados.
 */
public class FonteDadosBancoStrategy implements FonteDadosCertificadoStrategy {

    // Futuramente injetaremos o Repositório JPA aqui
    // private final MembroRepository membroRepository;

    @Override
    public List<AlunoCertificado> obterAlunos() {
        // Futuro: membroRepository.findMembrosComContratoFinalizado();
        // E mapeia para AlunoCertificado
        return List.of();
    }
}
