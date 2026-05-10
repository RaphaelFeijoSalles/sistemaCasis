package br.edu.utfpr.casis.backend.service.strategy;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;

import java.util.List;

/**
 * Interface que define o contrato para as estratégias de obtenção de dados de participantes.
 * Implementa o padrão de projeto Strategy para permitir diferentes fontes de dados
 * (ex: Planilha CSV, Banco de Dados, Integração com API externa) sem alterar o
 * serviço principal de emissão.
 */
public interface FonteDadosCertificadoStrategy {
    
    /**
     * Extrai ou consulta os dados e retorna uma lista padronizada de participantes.
     *
     * @return Uma lista de instâncias de {@link AlunoCertificado}.
     */
    List<AlunoCertificado> obterAlunos();
}