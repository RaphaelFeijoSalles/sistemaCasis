package br.edu.utfpr.casis.backend.controller;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.service.EmissaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificados")
@CrossOrigin(origins = {"https://sistema-casis.vercel.app/", "http://localhost:5173"})
@RequiredArgsConstructor
public class CertificadoController {

    private final EmissaoService emissaoService;

    // Puxa a senha das variáveis de ambiente (se não achar, usa 'senha_padrao' no localhost)
    @Value("${API_SECRET:senha_padrao_local}")
    private String apiSecret;

    @PostMapping(value = "/emitir-lote", consumes = "multipart/form-data")
    public ResponseEntity<?> emitirCertificadosEmLote(
            // O Spring pega a senha que o usuário digitou no React
            @RequestHeader(value = "X-API-KEY", required = false) String chaveAcesso,
            @ModelAttribute EmissaoLoteEventoRequestDTO requestDTO) {

        // A ApiSecret é usada para comparação
        if (chaveAcesso == null || !chaveAcesso.equals(apiSecret)) {
            return ResponseEntity.status(403).body("Acesso negado: Chave de autorização inválida.");
        }

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

    @PostMapping("/emitir-unitario")
    public ResponseEntity<?> emitirCertificadoIndividual(
            @RequestHeader(value = "X-API-KEY", required = false) String chaveAcesso,
            @RequestBody br.edu.utfpr.casis.backend.dto.EmissaoIndividualRequestDTO requestDTO) {

        if (chaveAcesso == null || !chaveAcesso.equals(apiSecret)) {
            return ResponseEntity.status(403).body("Acesso negado: Chave de autorização inválida.");
        }

        try {
            var relatorio = emissaoService.processarEmissaoIndividual(requestDTO);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro crítico: " + e.getMessage());
        }
    }

}