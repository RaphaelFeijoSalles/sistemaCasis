package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import com.lowagie.text.DocumentException;
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
 * Serviço responsável por gerar arquivos PDF a partir de templates HTML renderizados pelo Thymeleaf.
 * Funde os dados dinâmicos do aluno e evento com o layout visual para criar o certificado final em memória.
 */
@Service
public class PdfService {

    private final SpringTemplateEngine templateEngine;

    /**
     * Construtor com injeção de dependência para o motor de templates do Thymeleaf.
     *
     * @param templateEngine A instância do SpringTemplateEngine.
     */
    public PdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Gera o certificado em formato PDF para um participante específico em um evento.
     * Renderiza um template HTML com as informações do aluno e evento e, em seguida,
     * converte esse HTML em um array de bytes PDF usando o Flying Saucer.
     *
     * @param aluno Os dados do participante (nome, RA, e-mail) a constar no certificado.
     * @param dadosEvento Os dados do evento (nome, data, carga horária).
     * @return Um array de bytes que representa o arquivo PDF do certificado.
     * @throws RuntimeException Se ocorrer erro na leitura da imagem de fundo ou na geração do PDF.
     */
    public byte[] gerarCertificado(AlunoCertificado aluno, EmissaoLoteEventoRequestDTO dadosEvento) {

        // 1. Configura as variáveis que serão injetadas no HTML
        Context context = new Context();
        context.setVariable("nomeAluno", aluno.getNome());
        context.setVariable("nomeEvento", dadosEvento.nomeEvento());
        context.setVariable("cargaHoraria", dadosEvento.cargaHoraria());

        // Formata a data de realização (Ex: 15 de Março de 2026)
        DateTimeFormatter diaMesAno = DateTimeFormatter.ofPattern("dd' de 'MMMM' de 'yyyy", Locale.of("pt", "BR"));
        context.setVariable("dataRealizacao", dadosEvento.dataRealizacao().format(diaMesAno));

        // Formata a data automática rodapé ("Londrina, 15 de Março de 2026")
        DateTimeFormatter cidadeDiaMesAno = DateTimeFormatter.ofPattern("'Londrina, 'dd' de 'MMMM' de 'yyyy", Locale.of("pt", "BR"));
        context.setVariable("dataEmissao", LocalDate.now().format(cidadeDiaMesAno));

        // Lê a imagem da pasta resources e converte para Base64 em memória
        try (InputStream is = new ClassPathResource("static/images/bg_certificado_evento.png").getInputStream()) {
            String base64Image = Base64.getEncoder().encodeToString(is.readAllBytes());
            context.setVariable("imagemFundoBase64", "data:image/png;base64," + base64Image);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar a imagem de fundo do certificado.", e);
        }

        // Além disso, vamos garantir que o RA seja passado pro HTML:
        context.setVariable("ra", aluno.getRa() != null ? aluno.getRa() : "N/A");

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