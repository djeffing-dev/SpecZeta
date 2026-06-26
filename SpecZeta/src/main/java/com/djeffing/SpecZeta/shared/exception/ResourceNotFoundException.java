package com.djeffing.SpecZeta.shared.exception;

public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construit l'exception avec un message libre.
     * Préférer le constructeur à trois arguments quand on identifie une ressource par clé.
     *
     * @param message description de l'erreur affichée à l'utilisateur
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construit un message structuré du type {@code "Annonce introuvable (id = 42)"}.
     * Utilisé dans les services pour signaler qu'une recherche par identifiant n'a rien donné.
     *
     * @param resource nom de l'entité concernée (ex. « Annonce »)
     * @param field    nom du champ de recherche (ex. « id »)
     * @param value    valeur recherchée
     */
    public ResourceNotFoundException(String resource, String field, Object value) {
        super("%s introuvable (%s = %s)".formatted(resource, field, value));
    }
}
