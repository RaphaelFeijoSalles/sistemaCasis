package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa os textos do e-mail (F4: CA12) sobre o template real, sem Spring e sem chamar o Gmail.
 */
class EmailServiceTest {

    private final EmailService emailService = criarEmailService();

    private static EmailService criarEmailService() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        EmailService service = new EmailService(engine, GestaoDeTeste.GESTAO);
        ReflectionTestUtils.setField(service, "remetenteOficial", "contato@exemplo.com");
        return service;
    }

    @Test
    void corpoUsaNomeESiglaDaGestao() {
        String corpo = emailService.montarCorpoEmail("Ana Silva", "Semana Acadêmica")
                .replaceAll("\\s+", " ").replaceAll("</?span>", "");

        assertThat(corpo)
                .contains("<title>Seu Certificado do DACOMP</title>")
                .contains("<h1>Diretório Acadêmico de Computação</h1>")
                .contains("Sistema Oficial de Certificados do DACOMP")
                .contains("DACOMP UTFPR-LD")
                .contains("<strong>Ana Silva</strong>")
                .contains("<strong>Semana Acadêmica</strong>")
                .contains("contato@exemplo.com")
                .doesNotContain("CASIS")
                .doesNotContain("Centro Acadêmico de Sistemas de Informação");
    }

    @Test
    void assuntoMantemOMascote() {
        assertThat(emailService.montarAssunto("Semana Acadêmica")).isEqualTo("Seu Certificado chegou! 🐍 - Semana Acadêmica");
    }

    @Test
    void remetenteUsaNomeESiglaDaGestao() {
        assertThat(emailService.montarNomeRemetente()).isEqualTo("Diretório Acadêmico de Computação - DACOMP");
    }
}
