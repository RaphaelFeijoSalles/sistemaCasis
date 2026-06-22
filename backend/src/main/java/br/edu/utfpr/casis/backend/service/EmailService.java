package br.edu.utfpr.casis.backend.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

/**
 * Serviço responsável pela geração e envio de e-mails com certificados anexados.
 * Utiliza o Thymeleaf para renderizar o corpo do e-mail e a API oficial do Gmail para o envio via HTTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final TemplateEngine templateEngine;

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GOOGLE_REFRESH_TOKEN}")
    private String refreshToken;

    /**
     * E-mail de contato, usado para assinatura no corpo do HTML.
     */
    @Value("${spring.mail.username}")
    private String remetenteOficial;

    /**
     * Inicializa o serviço do Gmail usando as credenciais OAuth 2.0.
     */
    private Gmail obterServicoGmail() {
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Gmail.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("CASIS Certificados")
                .build();
    }

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
        log.info("Iniciando o envio do certificado via Gmail API para: {}", destinatario);
        try {
            // Instancia uma sessão nativa do Jakarta para construir a mensagem
            Session session = Session.getDefaultInstance(new Properties(), null);
            MimeMessage mimeMessage = new MimeMessage(session);

            // True pois o email deve ser enviado com multipart (permite anexos)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(remetenteOficial, "Centro Acadêmico de SI - CASIS");
            helper.setTo(destinatario);
            helper.setSubject("Seu Certificado chegou! 🐍 - " + nomeEvento);

            String corpoEmail = this.montarCorpoEmail(nomeAluno, nomeEvento);
            helper.setText(corpoEmail, true);

            // Nomeia o arquivo dinamicamente (Ex: Certificado_Raphael_Feijo.pdf)
            String nomeArquivo = "Certificado_" + nomeAluno.replaceAll("\\s+", "_") + ".pdf";
            helper.addAttachment(nomeArquivo, new ByteArrayResource(pdfAnexo));

            // Extrai a mensagem pronta em formato byte array
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();

            // Converte para Base64 URL Safe e encapsula na classe Message da API do Google
            String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);
            Message message = new Message();
            message.setRaw(encodedEmail);

            // Dispara a requisição HTTP POST para o Google (bypass de SMTP bloqueado)
            Gmail gmailService = obterServicoGmail();
            gmailService.users().messages().send("me", message).execute();

            log.info("E-mail com certificado enviado com sucesso para: {}", destinatario);

        } catch (Exception e) {
            log.error("Erro fatal ao tentar enviar e-mail para o destinatário: {}", destinatario, e);
            throw new RuntimeException("Erro fatal ao tentar enviar e-mail para o destinatário: " + destinatario, e);
        }
    }
}