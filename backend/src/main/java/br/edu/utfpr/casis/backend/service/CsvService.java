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
     * Extrai a lista de alunos a partir de um arquivo CSV de presença.
     * O arquivo CSV deve conter, pelo menos, Nome e E-mail nas primeiras colunas.
     * Opcionalmente, o RA pode estar na terceira coluna.
     *
     * @param arquivoCsv O arquivo CSV (MultipartFile) carregado via requisição HTTP.
     * @return Uma lista de objetos {@link AlunoCertificado} representando os participantes.
     * @throws RuntimeException Se ocorrer um erro durante a leitura ou processamento do arquivo.
     */
    public List<AlunoCertificado> extrairAlunos(MultipartFile arquivoCsv) {
        List<AlunoCertificado> alunos = new ArrayList<>();
        log.info("Iniciando a extração de alunos a partir de arquivo CSV.");

        try (Reader reader = new InputStreamReader(arquivoCsv.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

            String[] linha;
            while ((linha = csvReader.readNext()) != null) {
                if (linha.length >= 2 && !linha[0].trim().isEmpty()) {

                    AlunoCertificadoEvento aluno = AlunoCertificadoEvento.builder()
                            .nome(linha[0].trim())
                            .email(linha[1].trim())
                            .ra(linha.length >= 3 ? linha[2].trim() : null)
                            .tipoParticipacao(TipoParticipacao.OUVINTE) // Valor padrão para emissões em lote de forms comuns
                            .build();

                    alunos.add(aluno);
                }
            }
            log.info("Leitura de CSV concluída. {} alunos mapeados com sucesso.", alunos.size());

        } catch (Exception e) {
            log.error("Erro na conversão do arquivo CSV.", e);
            throw new RuntimeException("Falha ao ler a planilha. Verifique se o formato é um CSV válido.", e);
        }

        return alunos;
    }
}