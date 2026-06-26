package com.djeffing.SpecZeta.shared.exception;

public class StorageException extends RuntimeException {

    /**
     * Lancée quand une opération sur le stockage distant échoue de façon
     * irrécupérable (Dropbox 5xx persistant après retry, panne réseau,
     * réponse inattendue). Mappée en HTTP 503 par le {@code GlobalExceptionHandler}
     * avec un message générique pour ne pas exposer la cause technique au client.
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(String message) {
        super(message);
    }
}
