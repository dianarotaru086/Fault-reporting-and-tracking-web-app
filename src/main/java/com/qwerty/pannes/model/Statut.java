package com.qwerty.pannes.model;

public enum Statut {
    NEW("New"),
    IN_PROGRESS("In progress"),
    DONE("Done");

    private final String libelle;

    Statut(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
