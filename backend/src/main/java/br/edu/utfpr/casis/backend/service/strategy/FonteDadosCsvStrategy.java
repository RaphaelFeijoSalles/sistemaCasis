package br.edu.utfpr.casis.backend.service.strategy;

import br.edu.utfpr.casis.backend.model.AlunoCertificado;
import br.edu.utfpr.casis.backend.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Implementação da estratégia de extração de dados via arquivo CSV.
 * Utilizada majoritariamente para a emissão de certificados de Eventos isolados.
 */
@RequiredArgsConstructor
public class FonteDadosCsvStrategy implements FonteDadosCertificadoStrategy {

    private final MultipartFile arquivoCsv;
    private final CsvService csvService;

    /**
     * Delega a extração física do arquivo para o CsvService.
     * * @return Lista padronizada de alunos extraída da planilha.
     */
    @Override
    public List<AlunoCertificado> obterAlunos() {
        return csvService.extrairAlunos(arquivoCsv);
    }
}