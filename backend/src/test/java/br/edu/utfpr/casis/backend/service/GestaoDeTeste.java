package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.config.GestaoProperties;

import java.util.List;
import java.util.Map;

/**
 * Gestão DACOMP com os dois cursos (SI e CDIA), igual ao padrão do {@code application.properties}, para testes sem Spring.
 */
final class GestaoDeTeste {

    static final String NOME_SI = "Bacharelado em Sistemas de Informação";
    static final String NOME_CDIA = "Bacharelado em Ciência de Dados e Inteligência Artificial";

    static final GestaoProperties GESTAO = new GestaoProperties(
            "Diretório Acadêmico de Computação", "DACOMP", "Londrina", "static/images/bg_certificado_evento.png", Map.of(
            "SI", new GestaoProperties.Curso(NOME_SI, "Sistemas de Informação",
                    List.of("SI", "Sistemas de Informação")),
            "CDIA", new GestaoProperties.Curso(NOME_CDIA, "Ciência de Dados e Inteligência Artificial",
                    List.of("CDIA", "Ciência de Dados e Inteligência Artificial"))));

    private GestaoDeTeste() {
    }

    static InterpretadorCurso interpretador() {
        return new InterpretadorCurso(GESTAO);
    }
}
