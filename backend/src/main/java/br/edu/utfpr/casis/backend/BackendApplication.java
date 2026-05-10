package br.edu.utfpr.casis.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal responsável por inicializar a aplicação Spring Boot do backend do sistema CASIS.
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Método principal que serve como ponto de entrada para a aplicação.
     *
     * @param args argumentos de linha de comando.
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}