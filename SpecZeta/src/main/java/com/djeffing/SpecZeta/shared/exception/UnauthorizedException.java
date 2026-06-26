package com.djeffing.SpecZeta.shared.exception;

public class UnauthorizedException extends RuntimeException {

    /**
     * Lancée quand un utilisateur tente une action sans authentification valide
     * ou avec un token expiré. Mappée en HTTP 401 par le {@code GlobalExceptionHandler}.
     *
     * @param message raison de l'échec (ex. « Token expiré », « JWT manquant »)
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
