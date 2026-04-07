package br.edu.utfpr.casis.backend.controller;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.service.EmissaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificados")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CertificadoController {

    private final EmissaoService emissaoService;

    @PostMapping(value = "/emitir-lote", consumes = "multipart/form-data")
    public ResponseEntity<String> emitirCertificadosEmLote(@ModelAttribute EmissaoLoteEventoRequestDTO requestDTO) {

        if (requestDTO.arquivoCsv() == null || requestDTO.arquivoCsv().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: O arquivo CSV é obrigatório.");
        }

        try {
            // Toda a lógica pesada foi abstraída para o Service
            emissaoService.processarEmissao(requestDTO);

            return ResponseEntity.ok(
                    String.format("Lote em processamento com sucesso para o evento: %s", requestDTO.nomeEvento())
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro crítico: " + e.getMessage());
        }
    }
}