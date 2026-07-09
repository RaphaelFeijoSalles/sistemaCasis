package br.edu.utfpr.casis.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CargaHorariaFormatterTest {

    @Test
    void deveFormatarDuracaoParaPadraoDaPlanilha() {
        assertEquals("02:00:00", CargaHorariaFormatter.formatarDuracaoPlanilha(2.0));
        assertEquals("01:30:00", CargaHorariaFormatter.formatarDuracaoPlanilha(1.5));
        assertEquals("10:45:00", CargaHorariaFormatter.formatarDuracaoPlanilha(10.75));
    }

    @Test
    void deveNormalizarArredondamentoDeMinutos() {
        assertEquals("02:00:00", CargaHorariaFormatter.formatarDuracaoPlanilha(1.999));
        assertEquals("2 horas", CargaHorariaFormatter.formatarTexto(1.999));
    }

    @Test
    void deveFormatarTextoParaCertificado() {
        assertEquals("1 hora e 30 minutos", CargaHorariaFormatter.formatarTexto(1.5));
        assertEquals("45 minutos", CargaHorariaFormatter.formatarTexto(0.75));
        assertEquals("0 horas", CargaHorariaFormatter.formatarTexto(null));
    }
}
