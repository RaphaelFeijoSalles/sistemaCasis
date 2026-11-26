package br.edu.utfpr.casis.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o binding de {@code certificado.*} (F4) sobre o {@code application.properties} real, sem subir o Spring.
 * O Spring lê {@code .properties} como ISO-8859-1: os acentos precisam chegar corretos.
 */
class GestaoPropertiesTest {

    private GestaoProperties carregar(PropertySource<?> fonte) {
        return new Binder(ConfigurationPropertySources.from(fonte))
                .bind("certificado", GestaoProperties.class)
                .get();
    }

    private GestaoProperties carregarApplicationProperties() throws Exception {
        List<PropertySource<?>> fontes = new PropertiesPropertySourceLoader()
                .load("application", new ClassPathResource("application.properties"));
        return carregar(fontes.getFirst());
    }

    @Test
    void padroesDaGestaoDacompComAcentosCorretos() throws Exception {
        GestaoProperties gestao = carregarApplicationProperties();

        assertThat(gestao.nome()).isEqualTo("Diretório Acadêmico de Computação");
        assertThat(gestao.sigla()).isEqualTo("DACOMP");
        assertThat(gestao.cidade()).isEqualTo("Londrina");
        assertThat(gestao.imagemFundo()).isEqualTo("static/images/bg_certificado_evento.png");
    }

    @Test
    void cursosPadraoSiECdia() throws Exception {
        GestaoProperties gestao = carregarApplicationProperties();

        assertThat(gestao.cursos()).containsOnlyKeys("SI", "CDIA");

        GestaoProperties.Curso si = gestao.cursos().get("SI");
        assertThat(si.nomeCompleto()).isEqualTo("Bacharelado em Sistemas de Informação");
        assertThat(si.nomeCurto()).isEqualTo("Sistemas de Informação");
        assertThat(si.apelidos()).containsExactly("SI", "Sistemas de Informação");

        GestaoProperties.Curso cdia = gestao.cursos().get("CDIA");
        assertThat(cdia.nomeCompleto()).isEqualTo("Bacharelado em Ciência de Dados e Inteligência Artificial");
        assertThat(cdia.nomeCurto()).isEqualTo("Ciência de Dados e Inteligência Artificial");
        assertThat(cdia.apelidos()).containsExactly("CDIA", "Ciência de Dados e Inteligência Artificial");
    }

    @Test
    void codigoDoCursoViraMaiusculoMesmoVindoDeVariavelDeAmbiente() {
        GestaoProperties gestao = carregar(new MapPropertySource("teste", Map.of(
                "certificado.nome", "X",
                "certificado.sigla", "X",
                "certificado.cidade", "X",
                "certificado.imagem-fundo", "x.png",
                "certificado.cursos.es.nome-completo", "Bacharelado em Engenharia de Software",
                "certificado.cursos.es.nome-curto", "Engenharia de Software",
                "certificado.cursos.es.apelidos", "ES")));

        assertThat(gestao.cursos()).containsOnlyKeys("ES");
    }
}
