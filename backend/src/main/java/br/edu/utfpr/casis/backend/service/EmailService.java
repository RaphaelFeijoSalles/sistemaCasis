package br.edu.utfpr.casis.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // Puxa o e-mail remetente lá do application.properties
    @Value("${spring.mail.username}")
    private String remetenteOficial;

    public String montarCorpoEmail(String nomeAluno, String nomeEvento) {
        Context context = new Context();
        context.setVariable("nome_aluno", nomeAluno);
        context.setVariable("nome_evento", nomeEvento);
        context.setVariable("email_contato", remetenteOficial);
        return templateEngine.process("certificado-email", context);
    }

    public void enviarCertificado(String destinatario, String nomeAluno, String nomeEvento, byte[] pdfAnexo) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();

            // True pois o email deve ser enviado com multipart (permite anexos)
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(remetenteOficial, "Centro Acadêmico de SI - CASIS");
            helper.setTo(destinatario);
            helper.setSubject("Seu Certificado chegou! 🐍 - " + nomeEvento);

            String corpoEmail = this.montarCorpoEmail(nomeAluno, nomeEvento);

            helper.setText(corpoEmail, true);

            // Nomeia o arquivo dinamicamente (Ex: Certificado_Raphael_Feijo.pdf)
            String nomeArquivo = "Certificado_" + nomeAluno.replaceAll("\\s+", "_") + ".pdf";
            helper.addAttachment(nomeArquivo, new ByteArrayResource(pdfAnexo));

            mailSender.send(mensagem);
            log.info("E-mail enviado com sucesso para: {}", destinatario);

        } catch (Exception e) {
            log.error("Erro fatal ao tentar enviar e-mail para: {}", destinatario, e);
        }
    }
}