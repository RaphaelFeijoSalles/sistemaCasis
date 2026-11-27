package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.config.GestaoProperties;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Interpreta o curso de um participante (F4), sem efeitos colaterais.
 * <p>
 * Os valores aceitos são os apelidos de cada curso em {@code certificado.cursos.*.apelidos}
 * (ex.: {@code SI} ou {@code Sistemas de Informação}), comparados sem diferenciar maiúsculas,
 * acentos nem espaços repetidos.
 */
@Component
public class InterpretadorCurso {

    private final Map<String, GestaoProperties.Curso> cursosPorCodigo;
    private final Map<String, String> codigoPorApelido = new HashMap<>();

    /**
     * Monta o índice de apelidos a partir dos cursos configurados.
     *
     * @param gestao dados da gestão, com os cursos por código.
     */
    public InterpretadorCurso(GestaoProperties gestao) {
        this.cursosPorCodigo = gestao.cursos();
        cursosPorCodigo.forEach((codigo, curso) ->
                curso.apelidos().forEach(apelido -> codigoPorApelido.put(normalizar(apelido), codigo)));
    }

    /**
     * Converte o texto de uma célula (ou do formulário) no código do curso.
     *
     * @param valor texto informado, como veio do CSV ou da requisição.
     * @return o código do curso (ex.: {@code "SI"}), ou vazio se o valor for nulo, em branco
     *         ou não corresponder a nenhum apelido. Quem chama decide o que fazer com o vazio.
     */
    public Optional<String> interpretar(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(codigoPorApelido.get(normalizar(valor)));
    }

    /**
     * Devolve os dados de um curso pelo código, sem diferenciar maiúsculas.
     *
     * @param codigo código do curso (ex.: {@code "SI"}).
     * @return o curso configurado, ou vazio se o código for nulo ou desconhecido.
     */
    public Optional<GestaoProperties.Curso> curso(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cursosPorCodigo.get(codigo.trim().toUpperCase(Locale.ROOT)));
    }

    /** Minúsculas, sem acentos, sem espaços nas bordas e com espaços internos reduzidos a um. */
    private static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.replace("\uFEFF", ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
