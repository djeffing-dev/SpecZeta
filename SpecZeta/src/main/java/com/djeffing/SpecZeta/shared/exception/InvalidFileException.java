package com.djeffing.SpecZeta.shared.exception;

public class InvalidFileException extends RuntimeException {

    /**
     * Lancée quand un fichier reçu ne satisfait pas les règles métier :
     * fichier vide, mauvais type MIME (ex. PDF reçu là où une image est attendue),
     * taille supérieure au plafond, ou nombre de fichiers hors fourchette
     * (ex. moins de 3 photos pour une annonce).
     * Mappée en HTTP 400 par le {@code GlobalExceptionHandler}.
     */
    public InvalidFileException(String message) {
        super(message);
    }
}
