package br.edu.utfpr.casis.backend.controller;

import br.edu.utfpr.casis.backend.dto.EmissaoLoteEventoRequestDTO;
import br.edu.utfpr.casis.backend.service.EmissaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST responsável por gerir as requisições de emissão de certificados.
 * Suporta emissão em lote via CSV e emissão unitária.
 */
@Slf4j
@RestController
@RequestMapping("/api/certificados")
@CrossOrigin(origins = {"https://sistema-casis.vercel.app/", "http://localhost:5173"})
@RequiredArgsConstructor
public class CertificadoController {

    private final EmissaoService emissaoService;

    /**
     * Chave de acesso lida das variáveis de ambiente.
     * Utilizada para autenticação simples baseada em cabeçalho.
     */
    @Value("${API_SECRET:senha_padrao_local}")
    private String apiSecret;

    /**
     * Processa a emissão de certificados em lote utilizando um arquivo CSV.
     *
     * @param chaveAcesso chave de autorização fornecida no cabeçalho X-API-KEY.
     * @param requestDTO DTO contendo os dados do evento e o arquivo CSV de participantes.
     * @return um {@link ResponseEntity} contendo o relatório de emissão em caso de sucesso,
     *         ou uma mensagem de erro com o respectivo status HTTP em caso de falha.
     */
    @PostMapping(value = "/emitir-lote", consumes = "multipart/form-data")
    public ResponseEntity<?> emitirCertificadosEmLote(
            @RequestHeader(value = "X-API-KEY", required = false) String chaveAcesso,
            @ModelAttribute EmissaoLoteEventoRequestDTO requestDTO) {

        log.info("Iniciando requisição de emissão em lote para o evento: {}", requestDTO.nomeEvento());

        if (chaveAcesso == null || !chaveAcesso.equals(apiSecret)) {
            log.warn("Tentativa de acesso negado. Chave inválida fornecida.");
            return ResponseEntity.status(403).body("Acesso negado: Chave de autorização inválida.");
        }

        if (requestDTO.arquivoCsv() == null || requestDTO.arquivoCsv().isEmpty()) {
            log.warn("Arquivo CSV ausente na requisição de emissão em lote.");
            return ResponseEntity.badRequest().body("Erro: O arquivo CSV é obrigatório.");
        }

        try {
            var relatorio = emissaoService.processarEmissao(requestDTO);
            log.info("Processamento de emissão em lote concluído com sucesso.");
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            log.error("Erro crítico durante a emissão em lote: ", e);
            return ResponseEntity.internalServerError().body("Erro crítico: " + e.getMessage());
        }
    }

    /**
     * Processa a emissão de um único certificado.
     *
     * @param chaveAcesso chave de autorização fornecida no cabeçalho X-API-KEY.
     * @param requestDTO DTO contendo os dados do participante e do evento.
     * @return um {@link ResponseEntity} contendo o relatório de emissão em caso de sucesso,
     *         ou uma mensagem de erro com o respectivo status HTTP em caso de falha.
     */
    @PostMapping("/emitir-unitario")
    public ResponseEntity<?> emitirCertificadoIndividual(
            @RequestHeader(value = "X-API-KEY", required = false) String chaveAcesso,
            @RequestBody br.edu.utfpr.casis.backend.dto.EmissaoIndividualRequestDTO requestDTO) {

        log.info("Iniciando requisição de emissão unitária para o participante: {}", requestDTO.nomeParticipante());

        if (chaveAcesso == null || !chaveAcesso.equals(apiSecret)) {
            log.warn("Tentativa de acesso negado na emissão unitária. Chave inválida fornecida.");
            return ResponseEntity.status(403).body("Acesso negado: Chave de autorização inválida.");
        }

        try {
            var relatorio = emissaoService.processarEmissaoIndividual(requestDTO);
            log.info("Processamento de emissão unitária concluído com sucesso.");
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            log.error("Erro crítico durante a emissão unitária: ", e);
            return ResponseEntity.internalServerError().body("Erro crítico: " + e.getMessage());
        }
    }

}