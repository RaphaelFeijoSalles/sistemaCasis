package br.edu.utfpr.casis.backend.service;

/**
 * Regra única para o nome do arquivo PDF de um certificado.
 * <p>
 * Esse nome é a chave da anti-duplicação no Google Drive: alterar o formato
 * faz o sistema reemitir certificados já enviados.
 */
final class NomeArquivoCertificado {

    private NomeArquivoCertificado() {
    }

    /**
     * Gera o nome do arquivo do certificado a partir do nome do participante.
     * Remove os espaços das bordas e troca cada sequência de espaços por {@code _}.
     *
     * @param nomeParticipante nome do participante.
     * @return nome do arquivo no formato {@code Certificado_Nome_Sobrenome.pdf}.
     */
    static String gerar(String nomeParticipante) {
        return "Certificado_" + nomeParticipante.trim().replaceAll("\\s+", "_") + ".pdf";
    }
}
