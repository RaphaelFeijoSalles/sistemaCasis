package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.model.AlunoCertificadoEvento;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o certificado (F4: CA2, CA3) sobre o template real, sem Spring e sem Google.
 */
class PdfServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 20);

    private final PdfService pdfService = new PdfService(criarEngine(), GestaoDeTeste.GESTAO, GestaoDeTeste.interpretador());

    private static SpringTemplateEngine criarEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private AlunoCertificado aluno(String ra) {
        return AlunoCertificadoEvento.builder().nome("Ana Silva").email("ana@alunos.utfpr.edu.br").ra(ra).build();
    }

    private EmissaoLoteEventoRequestDTO evento(String curso) {
        return new EmissaoLoteEventoRequestDTO("Semana Acadêmica", DATA, 1.5, curso, null);
    }

    /** Texto do HTML sem quebras, espaços repetidos nem {@code <span>}, para comparar frases inteiras. */
    private String html(AlunoCertificado aluno, EmissaoLoteEventoRequestDTO evento) {
        return pdfService.montarHtml(aluno, evento).replaceAll("\\s+", " ").replaceAll("</?span>", "");
    }

    @Test
    void certificadoComGestaoECursoSi() {
        String html = html(aluno("12345"), evento("SI"));

        assertThat(html)
                .contains("Ana Silva")
                .contains("portador(a) do RA <strong>12345</strong> aluno(a) do curso de Bacharelado em Sistemas de Informação,")
                .contains("participou da <strong>Semana Acadêmica</strong>")
                .contains("promovida pelo Diretório Acadêmico de Computação (DACOMP)")
                .contains("20 de novembro de 2026")
                .contains("<strong>1 hora e 30 minutos</strong>")
                .containsPattern("Londrina, \\d{2} de \\p{L}+ de \\d{4}")
                .doesNotContain("CASIS")
                .doesNotContain("Centro Acadêmico de Sistemas de Informação");
    }

    @Test
    void certificadoComCursoCdia() {
        assertThat(html(aluno("12345"), evento("CDIA")))
                .contains("aluno(a) do curso de Bacharelado em Ciência de Dados e Inteligência Artificial,")
                .doesNotContain("Sistemas de Informação");
    }

    @Test
    void cursoAusenteUsaSi() {
        assertThat(html(aluno("12345"), evento(null)))
                .contains("aluno(a) do curso de Bacharelado em Sistemas de Informação,");
    }

    @Test
    void semRaOmiteOTrechoDoRa() {
        for (String ra : new String[]{null, "", "   "}) {
            String html = html(aluno(ra), evento("SI"));

            assertThat(html)
                    .doesNotContain("portador(a) do RA")
                    .doesNotContain("N/A")
                    .contains("<div class=\"texto-descritivo\"> aluno(a) do curso de Bacharelado em Sistemas de Informação,");
        }
    }

    @Test
    void usaAImagemDeFundoConfigurada() {
        assertThat(html(aluno("12345"), evento("SI"))).contains("src=\"data:image/png;base64,");
    }

    @Test
    void geraUmPdf() {
        byte[] pdf = pdfService.gerarCertificado(aluno("12345"), evento("CDIA"));

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }
}
