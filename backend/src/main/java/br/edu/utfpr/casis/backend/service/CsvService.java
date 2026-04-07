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
 * Serviço responsável por ler arquivos CSV e convertê-los na entidade padrão do sistema.
 */
@Slf4j
@Service
public class CsvService {

    /**
     * Processa a planilha de presença e extrai os dados fundamentais do aluno.
     * * @param arquivoCsv Arquivo binário em memória enviado via formulário.
     * @return Lista populada de AlunoCertificado, pronta para emissão.
     */
    public List<AlunoCertificado> extrairAlunos(MultipartFile arquivoCsv) {
        List<AlunoCertificado> alunos = new ArrayList<>();

        try (Reader reader = new InputStreamReader(arquivoCsv.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

            String[] linha;
            while ((linha = csvReader.readNext()) != null) {
                if (linha.length >= 2 && !linha[0].trim().isEmpty()) {

                    // Utiliza o Builder gerado pelo Lombok no AlunoCertificado
                    // Dentro do while do CsvService:

                    AlunoCertificadoEvento aluno = AlunoCertificadoEvento.builder()
                            .nome(linha[0].trim())
                            .email(linha[1].trim())
                            .documento(linha.length >= 3 ? linha[2].trim() : null)
                            .tipoParticipacao(TipoParticipacao.OUVINTE) // Valor padrão para emissões em lote de forms comuns
                            .build();

                    alunos.add(aluno);
                }
            }
            log.info("Leitura de CSV concluída. {} alunos mapeados.", alunos.size());

        } catch (Exception e) {
            log.error("Erro na conversão do arquivo CSV.", e);
            throw new RuntimeException("Falha ao ler a planilha. Verifique se o formato é um CSV válido.", e);
        }

        return alunos;
    }
}