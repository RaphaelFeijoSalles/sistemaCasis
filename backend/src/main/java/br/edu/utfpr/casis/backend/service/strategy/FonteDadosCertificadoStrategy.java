package br.edu.utfpr.casis.backend.service.strategy;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;

import java.util.List;

/**
 * Interface comum para qualquer fonte de dados de certificados.
 */
public interface FonteDadosCertificadoStrategy {
    List<AlunoCertificado> obterAlunos();
}