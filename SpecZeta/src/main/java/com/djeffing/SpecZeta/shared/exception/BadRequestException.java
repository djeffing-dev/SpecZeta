package com.djeffing.SpecZeta.shared.exception;

public class BadRequestException extends RuntimeException {

    /**
     * Lancée par les services lorsqu'une règle métier n'est pas respectée
     * (ex. moins de 3 photos sur une annonce, prix négatif, etc.).
     * Mappée en HTTP 400 par le {@code GlobalExceptionHandler}.
     *
     * @param message message d'erreur explicite à destination du client
     */
    public BadRequestException(String message) {
        super(message);
    }
}
