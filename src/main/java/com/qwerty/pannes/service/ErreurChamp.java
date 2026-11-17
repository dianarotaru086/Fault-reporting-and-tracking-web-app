package com.qwerty.pannes.service;

/** Une erreur liée à un champ précis du formulaire (ex. e-mail déjà utilisé). */
public class ErreurChamp extends RuntimeException {

    private final String champ;

    public ErreurChamp(String champ, String message) {
        super(message);
        this.champ = champ;
    }

    public String getChamp() {
        return champ;
    }
}