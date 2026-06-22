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

/**
 * Serviço responsável pela geração e envio de e-mails com certificados anexados.
 * Utiliza o Thymeleaf para renderizar o corpo do e-mail e o JavaMailSender para o envio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Endereço de e-mail do remetente, configurado nas propriedades da aplicação.
     */
    @Value("${spring.mail.username}")
    private String remetenteOficial;

    /**
     * Monta o corpo do e-mail em HTML a partir do template do Thymeleaf.
     * Injeta as variáveis dinâmicas no template.
     *
     * @param nomeAluno O nome do participante que receberá o certificado.
     * @param nomeEvento O nome do evento para o qual o certificado foi gerado.
     * @return O HTML do corpo do e-mail renderizado.
     */
    public String montarCorpoEmail(String nomeAluno, String nomeEvento) {
        log.debug("Montando o corpo do e-mail para o aluno: {} no evento: {}", nomeAluno, nomeEvento);
        Context context = new Context();
        context.setVariable("nome_aluno", nomeAluno);
        context.setVariable("nome_evento", nomeEvento);
        context.setVariable("email_contato", remetenteOficial);
        return templateEngine.process("certificado-email", context);
    }

    /**
     * Prepara a mensagem MIME e envia o certificado por e-mail com anexo em PDF.
     *
     * @param destinatario O endereço de e-mail do destinatário.
     * @param nomeAluno O nome completo do participante (usado no corpo do e-mail e nome do anexo).
     * @param nomeEvento O nome do evento (usado no assunto e no corpo do e-mail).
     * @param pdfAnexo Array de bytes correspondente ao PDF gerado do certificado.
     */
    public void enviarCertificado(String destinatario, String nomeAluno, String nomeEvento, byte[] pdfAnexo) {
        log.info("Iniciando o envio do certificado por e-mail para: {}", destinatario);
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
            log.info("E-mail com certificado enviado com sucesso para: {}", destinatario);

        } catch (Exception e) {
            log.error("Erro fatal ao tentar enviar e-mail para o destinatário: {}", destinatario, e);
            throw new RuntimeException("Erro fatal ao tentar enviar e-mail para o destinatário: " + destinatario, e);
        }
    }
}