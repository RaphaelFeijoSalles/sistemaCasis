package br.edu.utfpr.casis.backend.model;

/**
 * Enumeração que define os papéis que um participante pode assumir em um evento.
 */
public enum TipoParticipacao {
    /**
     * Participante que assistiu ao evento.
     */
    OUVINTE,

    /**
     * Participante que palestrou ou ministrou conteúdo no evento.
     */
    PALESTRANTE,

    /**
     * Participante que fez parte da equipe organizadora do evento.
     */
    ORGANIZADOR
}