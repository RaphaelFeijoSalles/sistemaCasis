package br.edu.utfpr.casis.backend.controller;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.service.EmissaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificados")
@CrossOrigin(origins = {"https://sistemacasis.onrender.com", "http://localhost:5173"})
@RequiredArgsConstructor
public class CertificadoController {

    private final EmissaoService emissaoService;

    // Puxa a senha das variáveis de ambiente (se não achar, usa 'senha_padrao' no localhost)
    @Value("${API_SECRET:senha_padrao_local}")
    String apiSecret;

    @PostMapping(value = "/emitir-lote", consumes = "multipart/form-data")
    public ResponseEntity<?> emitirCertificadosEmLote(@ModelAttribute EmissaoLoteEventoRequestDTO requestDTO) {


        if (requestDTO.arquivoCsv() == null || requestDTO.arquivoCsv().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: O arquivo CSV é obrigatório.");
        }

        try {
            // Guarda o relatório devolvido pelo serviço
            var relatorio = emissaoService.processarEmissao(requestDTO);

            // Devolve o JSON pro frontend
            return ResponseEntity.ok(relatorio);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro crítico: " + e.getMessage());
        }
    }
}