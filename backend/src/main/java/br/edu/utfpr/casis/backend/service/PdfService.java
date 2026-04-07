package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.core.io.ClassPathResource;
import java.util.Base64;
import java.io.InputStream;

/**
 * Motor de renderização. Funde os dados dinâmicos com o template HTML
 * e "plota" o resultado final em um arquivo PDF na memória RAM.
 */
@Service
public class PdfService {

    private final SpringTemplateEngine templateEngine;

    // Injeção de dependência via construtor (melhor do que @Autowired aqui)
    public PdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Gera um PDF individual para um aluno.
     * * @param aluno Dados extraídos do CSV
     * @param dadosEvento Dados preenchidos no frontend
     * @return Array de bytes representando o arquivo PDF
     */
    public byte[] gerarCertificado(AlunoCertificado aluno, EmissaoLoteEventoRequestDTO dadosEvento) {

        // 1. Configura as variáveis que serão injetadas no HTML
        Context context = new Context();
        context.setVariable("nomeAluno", aluno.getNome());
        context.setVariable("nomeEvento", dadosEvento.nomeEvento());
        context.setVariable("cargaHoraria", dadosEvento.cargaHoraria());

        // Formata a data automática rodapé ("Londrina, 15 de Março de 2026")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Londrina, 'dd' de 'MMMM' de 'yyyy", Locale.of("pt", "BR"));
        context.setVariable("dataEmissao", LocalDate.now().format(formatter));

        // Lê a imagem da pasta resources e converte para Base64 em memória
        try (InputStream is = new ClassPathResource("static/images/bg_certificado_evento.png").getInputStream()) {
            String base64Image = Base64.getEncoder().encodeToString(is.readAllBytes());
            context.setVariable("imagemFundoBase64", "data:image/png;base64," + base64Image);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar a imagem de fundo do certificado.", e);
        }

        // Além disso, vamos garantir que o RA seja passado pro HTML:
        context.setVariable("documento", aluno.getDocumento() != null ? aluno.getDocumento() : "N/A");

        // 2. Renderiza o HTML (Escolhe o arquivo .html baseado no tipo)
        String templateName = "certificado_evento";

        String htmlProcessado = templateEngine.process(templateName, context);

        // 3. Converte o HTML para PDF em memória usando ITextRenderer (OpenPDF/FlyingSaucer)
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(htmlProcessado);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();

        } catch (DocumentException e){
            throw new RuntimeException("Erro ao gerar o PDF de " + aluno.getNome(), e);
        }

        catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao gerar o PDF de " + aluno.getNome(), e);
        }
    }
}