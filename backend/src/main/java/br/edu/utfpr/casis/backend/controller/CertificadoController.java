package br.edu.utfpr.casis.backend.controller;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint principal da API REST para a gestão de certificados do CASIS.
 * Recebe chamadas do frontend em React (porta 5173).
 */
@RestController
@RequestMapping("/api/certificados")
@CrossOrigin(origins = "http://localhost:5173") // Permite requisições do Vite localmente
public class CertificadoController {

    // Injetar o CertificadoService via construtor depois

    /**
     * Recebe um formulário multipart do frontend com os dados do evento e o arquivo CSV,
     * e inicia o fluxo assíncrono (ou síncrono) de geração e envio.
     *
     * @param requestDTO O objeto populado automaticamente pelo Spring via @ModelAttribute
     * @return ResponseEntity com o status da operação
     */
    @PostMapping(value = "/emitir-lote", consumes = "multipart/form-data")
    public ResponseEntity<String> emitirCertificadosEmLote(@ModelAttribute EmissaoLoteRequestDTO requestDTO) {

        // 1. Validação básica (Fail Fast)
        if (requestDTO.getArquivoCsv() == null || requestDTO.getArquivoCsv().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: O arquivo CSV é obrigatório.");
        }

        try {
            // =========================================================================
            // TODO: Chamar o serviço aqui.
            // Ex: certificadoService.processarLote(requestDTO);
            // =========================================================================

            String nomeArquivo = requestDTO.getArquivoCsv().getOriginalFilename();

            return ResponseEntity.ok(
                    String.format("Lote recebido com sucesso! Processando arquivo %s para o evento: %s",
                            nomeArquivo, requestDTO.getNomeEvento())
            );

        } catch (Exception e) {
            // Em produção, implementar Logger e ControllerAdvice para tratar erros.
            return ResponseEntity.internalServerError().body("Erro ao processar lote: " + e.getMessage());
        }
    }
}