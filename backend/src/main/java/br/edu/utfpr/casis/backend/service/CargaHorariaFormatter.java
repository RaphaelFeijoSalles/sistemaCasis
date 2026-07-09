package br.edu.utfpr.casis.backend.service;

final class CargaHorariaFormatter {

    private CargaHorariaFormatter() {
    }

    static String formatarTexto(Double cargaHoraria) {
        int minutosTotais = calcularMinutosTotais(cargaHoraria);
        if (minutosTotais <= 0) {
            return "0 horas";
        }

        int horas = minutosTotais / 60;
        int minutos = minutosTotais % 60;
        StringBuilder texto = new StringBuilder();

        if (horas > 0) {
            texto.append(horas).append(horas == 1 ? " hora" : " horas");
        }

        if (minutos > 0) {
            if (horas > 0) {
                texto.append(" e ");
            }
            texto.append(minutos).append(minutos == 1 ? " minuto" : " minutos");
        }

        return texto.toString();
    }

    static String formatarDuracaoPlanilha(Double cargaHoraria) {
        int minutosTotais = calcularMinutosTotais(cargaHoraria);
        if (minutosTotais <= 0) {
            return "00:00:00";
        }

        int horas = minutosTotais / 60;
        int minutos = minutosTotais % 60;
        return String.format("%02d:%02d:00", horas, minutos);
    }

    private static int calcularMinutosTotais(Double cargaHoraria) {
        if (cargaHoraria == null || cargaHoraria <= 0) {
            return 0;
        }

        return (int) Math.round(cargaHoraria * 60);
    }
}
