package com.qwerty.pannes.service;

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