package br.edu.utfpr.casis.backend.service.strategy;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Estratégia de fonte de dados que obtém os participantes a partir de um arquivo CSV.
 * Utilizada majoritariamente para a emissão de certificados em lote para eventos.
 */
@RequiredArgsConstructor
public class FonteDadosCsvStrategy implements FonteDadosCertificadoStrategy {

    private final MultipartFile arquivoCsv;
    private final CsvService csvService;

    /**
     * Extrai a lista de alunos lendo o arquivo CSV fornecido, delegando a lógica
     * de parseamento para o {@link CsvService}.
     *
     * @return Uma lista de instâncias de {@link AlunoCertificado}.
     */
    @Override
    public List<AlunoCertificado> obterAlunos() {
        return csvService.extrairAlunos(arquivoCsv);
    }
}