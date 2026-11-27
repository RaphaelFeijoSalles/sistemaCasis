package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.config.GestaoProperties;
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
 * <p>
 * Desde a F4, o nome e a sigla da gestão, a cidade, a imagem de fundo e o curso vêm da configuração
 * {@code certificado.*} ({@link GestaoProperties}).
 */
@Service
public class PdfService {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final SpringTemplateEngine templateEngine;
    private final GestaoProperties gestao;
    private final InterpretadorCurso interpretadorCurso;

    /**
     * Construtor com injeção de dependência para o motor de templates e os dados da gestão.
     *
     * @param templateEngine A instância do SpringTemplateEngine.
     * @param gestao Dados da gestão (nome, sigla, cidade e imagem de fundo).
     * @param interpretadorCurso Cursos configurados, para obter o nome completo do curso.
     */
    public PdfService(SpringTemplateEngine templateEngine, GestaoProperties gestao, InterpretadorCurso interpretadorCurso) {
        this.templateEngine = templateEngine;
        this.gestao = gestao;
        this.interpretadorCurso = interpretadorCurso;
    }

    /**
     * Gera o certificado em formato PDF para um participante específico em um evento.
     * Renderiza um template HTML com as informações do aluno e evento e, em seguida,
     * converte esse HTML em um array de bytes PDF usando o Flying Saucer.
     *
     * @param aluno Os dados do participante (nome, RA, e-mail) a constar no certificado.
     * @param dadosEvento Os dados do evento (nome, datas de início e fim, carga horária) e o curso do participante em
     *                    {@code cursoPadrao} (código; ausente = "SI").
     * @return Um array de bytes que representa o arquivo PDF do certificado.
     * @throws RuntimeException Se ocorrer erro na leitura da imagem de fundo ou na geração do PDF.
     */
    public byte[] gerarCertificado(AlunoCertificado aluno, EmissaoLoteEventoRequestDTO dadosEvento) {

        String htmlProcessado = montarHtml(aluno, dadosEvento);

        // Converte o HTML para PDF em memória usando ITextRenderer (OpenPDF/FlyingSaucer)
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

    /**
     * Renderiza o HTML do certificado, antes da conversão para PDF.
     * Separado de {@link #gerarCertificado} para permitir testar o texto sem gerar o PDF.
     *
     * @param aluno Os dados do participante.
     * @param dadosEvento Os dados do evento e o curso do participante em {@code cursoPadrao}.
     * @return O HTML processado pelo Thymeleaf.
     * @throws DadosInvalidosException Se o curso não for um curso configurado.
     * @throws RuntimeException Se ocorrer erro na leitura da imagem de fundo.
     */
    public String montarHtml(AlunoCertificado aluno, EmissaoLoteEventoRequestDTO dadosEvento) {

        // 1. Configura as variáveis que serão injetadas no HTML
        Context context = new Context();
        context.setVariable("nomeAluno", aluno.getNome());
        context.setVariable("nomeEvento", dadosEvento.nomeEvento());
        context.setVariable("cargaHoraria", CargaHorariaFormatter.formatarTexto(dadosEvento.cargaHoraria()));

        // Dados da gestão (F4)
        context.setVariable("nomeGestao", gestao.nome());
        context.setVariable("siglaGestao", gestao.sigla());

        // Curso do participante (F4): código já validado; ausente = SI
        String codigoCurso = ValidadorParticipantes.resolverCurso(dadosEvento.cursoPadrao(), interpretadorCurso);
        context.setVariable("cursoNome", interpretadorCurso.curso(codigoCurso)
                .map(GestaoProperties.Curso::nomeCompleto)
                .orElseThrow(() -> new DadosInvalidosException("Curso inválido: " + codigoCurso)));

        // Período de realização (F3): "em 15 de março de 2026" ou "no período de 01 a 05 de abril de 2026"
        context.setVariable("periodoRealizacao",
                PeriodoFormatter.formatar(dadosEvento.dataRealizacao(), dadosEvento.dataFim()));

        DateTimeFormatter diaMesAno = DateTimeFormatter.ofPattern("dd' de 'MMMM' de 'yyyy", PT_BR);

        // Formata a data automática do rodapé ("Londrina, 15 de Março de 2026"), com a cidade configurada
        context.setVariable("dataEmissao", gestao.cidade() + ", " + LocalDate.now().format(diaMesAno));

        // Lê a imagem configurada da pasta resources e converte para Base64 em memória
        try (InputStream is = new ClassPathResource(gestao.imagemFundo()).getInputStream()) {
            String base64Image = Base64.getEncoder().encodeToString(is.readAllBytes());
            context.setVariable("imagemFundoBase64", "data:image/png;base64," + base64Image);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar a imagem de fundo do certificado.", e);
        }

        // RA: sem RA, o trecho "portador(a) do RA ..." some do certificado (F4)
        context.setVariable("ra", aluno.getRa() != null && !aluno.getRa().isBlank() ? aluno.getRa().trim() : null);

        // 2. Renderiza o HTML
        return templateEngine.process("certificado_evento", context);
    }
}
