package br.edu.utfpr.casis.backend.service;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.model.AlunoCertificadoEvento;
import br.edu.utfpr.casis.backend.model.TipoParticipacao;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por processar e extrair dados de arquivos CSV.
 * Converte as linhas do arquivo em instâncias de {@link AlunoCertificado}.
 */
@Slf4j
@Service
public class CsvService {

    /**
     * Classe interna para armazenar as posições das colunas mapeadas do CSV.
     */
    private static class HeaderMap {
        int indexNome = -1;
        int indexEmail = -1;
        int indexRa = -1;

        boolean isValido() {
            // Não obrigatoriedade do RA para proporcionar mais flexibilidade para outros tipos de emissões
            return indexNome != -1 && indexEmail != -1;

        }
    }

    /**
     * Extrai a lista de alunos a partir de um arquivo CSV de presença.
     * Lê o cabeçalho e mapeia dinamicamente as colunas necessárias.
     *
     * @param arquivoCsv O arquivo CSV (MultipartFile) carregado via requisição HTTP.
     * @return Uma lista de objetos {@link AlunoCertificado} representando os participantes.
     * @throws RuntimeException Se ocorrer um erro durante a leitura ou processamento do arquivo.
     */
    public List<AlunoCertificado> extrairAlunos(MultipartFile arquivoCsv) {
        List<AlunoCertificado> alunos = new ArrayList<>();
        log.info("Iniciando a extração de alunos a partir de arquivo CSV.");

        try (Reader reader = new InputStreamReader(arquivoCsv.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {

            // 1. Ler a primeira linha (Cabeçalho)
            String[] cabecalho = csvReader.readNext();
            if (cabecalho == null || cabecalho.length == 0) {
                throw new IllegalArgumentException("O arquivo CSV está vazio ou sem cabeçalho.");
            }

            // 2. Mapear as posições dinamicamente
            HeaderMap map = mapearCabecalhos(cabecalho);
            if (!map.isValido()) {
                throw new IllegalArgumentException("Cabeçalho inválido. Não foi possível identificar as colunas de 'Nome' e 'E-mail'.");
            }

            // 3. Processar as linhas de dados
            String[] linha;
            while ((linha = csvReader.readNext()) != null) {
                // Previne OutOfBounds caso uma linha venha incompleta
                if (linha.length > Math.max(map.indexNome, map.indexEmail)) {
                    String nome = linha[map.indexNome].trim();
                    String email = linha[map.indexEmail].trim();
                    
                    String ra = null;
                    if (map.indexRa != -1 && linha.length > map.indexRa) {
                        ra = linha[map.indexRa].trim();
                    }

                    if (!nome.isEmpty() && !email.isEmpty()) {
                        AlunoCertificadoEvento aluno = AlunoCertificadoEvento.builder()
                                .nome(nome)
                                .email(email)
                                .ra(ra)
                                .tipoParticipacao(TipoParticipacao.OUVINTE) // Valor padrão para emissões em lote de forms comuns
                                .build();

                        alunos.add(aluno);
                    }
                }
            }
            log.info("Leitura de CSV concluída. {} alunos mapeados com sucesso.", alunos.size());

        } catch (Exception e) {
            log.error("Erro na conversão do arquivo CSV.", e);
            throw new RuntimeException("Falha ao ler a planilha. Verifique se o formato é um CSV válido e os dados estão corretos.", e);
        }

        return alunos;
    }

    /**
     * Varre a primeira linha (cabeçalho) e tenta localizar onde estão as colunas chave.
     */
    private HeaderMap mapearCabecalhos(String[] cabecalho) {
        HeaderMap map = new HeaderMap();

        for (int i = 0; i < cabecalho.length; i++) {
            String colNorm = normalizarString(cabecalho[i]);

            if (map.indexNome == -1 && isNome(colNorm)) {
                map.indexNome = i;
            } else if (map.indexEmail == -1 && isEmail(colNorm)) {
                map.indexEmail = i;
            } else if (map.indexRa == -1 && isRa(colNorm)) {
                map.indexRa = i;
            }
        }
        
        log.info("Mapeamento do Header CSV -> Nome: [{}], Email: [{}], RA: [{}]", map.indexNome, map.indexEmail, map.indexRa);
        return map;
    }

    private boolean isNome(String col) {
        return col.equals("nome") ||
               col.equals("nomes") ||
               col.contains("nome completo") ||
               col.contains("nome do participante") ||
               col.contains("nome do aluno") ||
               col.contains("nome para certificado") ||
               col.equals("participante");
    }

    private boolean isEmail(String col) {
        return col.contains("email") ||
               col.contains("e-mail") ||
               col.contains("correio eletronico");
    }

    private boolean isRa(String col) {
        return col.equals("ra") ||
               col.equals("r.a") ||
               col.equals("r.a.") ||
               col.contains("registro academico") ||
               col.contains("matricula");
    }

    /**
     * Normaliza uma string: converte para minúscula, tira espaços das bordas e remove acentuações.
     * Exemplo: " Nome Completo " -> "nome completo", "Endereço de e-mail" -> "endereco de e-mail"
     */
    private String normalizarString(String str) {
        if (str == null) return "";
        String minuscula = str.trim().toLowerCase();
        // Normaliza e remove os diacríticos (acentos, cedilha, etc)
        return Normalizer.normalize(minuscula, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}