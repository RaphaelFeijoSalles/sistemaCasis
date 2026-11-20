package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NomeArquivoCertificadoTest {

    @Test
    void deveGerarNomeComUnderlineEntrePalavras() {
        assertEquals("Certificado_Ana_Silva.pdf", NomeArquivoCertificado.gerar("Ana Silva"));
    }

    @Test
    void deveRemoverEspacosDasBordas() {
        assertEquals("Certificado_Ana_Silva.pdf", NomeArquivoCertificado.gerar("  Ana Silva  "));
    }

    @Test
    void deveColapsarEspacosMultiplosETabulacoes() {
        assertEquals("Certificado_Ana_Maria_Silva.pdf", NomeArquivoCertificado.gerar("Ana   Maria\tSilva"));
    }

    @Test
    void deveManterAcentosEMaiusculas() {
        assertEquals("Certificado_João_DA_Conceição.pdf", NomeArquivoCertificado.gerar("João DA Conceição"));
    }
}
