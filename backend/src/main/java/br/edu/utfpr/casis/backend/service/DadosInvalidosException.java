package br.edu.utfpr.casis.backend.service;

/**
 * Indica que os dados enviados pela diretoria (formulário ou CSV) são inválidos.
 * Deve ser traduzida para HTTP 400 e nunca deve ocorrer depois de um efeito real
 * (PDF, e-mail, Drive ou Sheets).
 */
public class DadosInvalidosException extends RuntimeException {

    /**
     * Cria a exceção com uma mensagem legível para a diretoria.
     *
     * @param mensagem descrição do problema, em português.
     */
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção com uma mensagem legível e a causa original.
     *
     * @param mensagem descrição do problema, em português.
     * @param causa exceção que originou o problema.
     */
    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
