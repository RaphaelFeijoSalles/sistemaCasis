package br.edu.utfpr.casis.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dados da gestão do centro/diretório acadêmico que aparecem no certificado e no e-mail (F4).
 * <p>
 * Lidos de {@code certificado.*}: o padrão fica no {@code application.properties} e pode ser
 * sobrescrito por variável de ambiente (ex.: {@code CERTIFICADO_SIGLA}). Trocar de gestão é trocar
 * a imagem de fundo e estas propriedades, sem mexer em código.
 *
 * @param nome        nome por extenso (ex.: "Diretório Acadêmico de Computação").
 * @param sigla       sigla (ex.: "DACOMP").
 * @param cidade      cidade da linha de data do certificado (ex.: "Londrina").
 * @param imagemFundo caminho no classpath da imagem de fundo do certificado.
 * @param cursos      cursos atendidos, por código em maiúsculas (ex.: "SI", "CDIA").
 */
@ConfigurationProperties("certificado")
public record GestaoProperties(
        String nome,
        String sigla,
        String cidade,
        String imagemFundo,
        Map<String, Curso> cursos
) {

    /**
     * Normaliza os códigos dos cursos para maiúsculas, já que variáveis de ambiente chegam em minúsculas.
     */
    public GestaoProperties {
        Map<String, Curso> normalizados = new LinkedHashMap<>();
        if (cursos != null) {
            cursos.forEach((codigo, curso) -> normalizados.put(codigo.trim().toUpperCase(Locale.ROOT), curso));
        }
        cursos = Map.copyOf(normalizados);
    }

    /**
     * Um curso atendido pela gestão.
     *
     * @param nomeCompleto nome impresso no certificado (ex.: "Bacharelado em Sistemas de Informação").
     * @param nomeCurto    nome gravado no Sheets (ex.: "Sistemas de Informação").
     * @param apelidos     valores aceitos na coluna {@code Curso} do CSV, comparados sem caixa nem acento.
     */
    public record Curso(String nomeCompleto, String nomeCurto, List<String> apelidos) {

        /** Garante uma lista de apelidos imutável e nunca nula. */
        public Curso {
            apelidos = apelidos == null ? List.of() : List.copyOf(apelidos);
        }
    }
}
