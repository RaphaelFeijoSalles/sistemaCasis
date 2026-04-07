package br.edu.utfpr.casis.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Puxa o e-mail remetente lá do application.properties
    @Value("${spring.mail.username}")
    private String remetenteOficial;

    public void enviarCertificado(String destinatario, String nomeAluno, String nomeEvento, byte[] pdfAnexo) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();

            // True pois o email deve ser enviado com multipart (permite anexos)
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(remetenteOficial, "Centro Acadêmico de SI - CASIS");
            helper.setTo(destinatario);
            helper.setSubject("Seu Certificado chegou! 🐍 - " + nomeEvento);

            // Corpo do e-mail em HTML
            String corpoEmail = String.format(
                    "<h2 style='color: #4CAF50;'>Olá, %s!</h2>" +
                            "<p>Obrigado por participar do evento <strong>%s</strong>.</p>" +
                            "<p>Seu certificado oficial de participação já está disponível e segue em anexo neste e-mail (PDF).</p>" +
                            "<br><p>Abraços,<br><strong>Diretoria CASIS</strong></p>",
                    nomeAluno, nomeEvento
            );

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