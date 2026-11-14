package com.qwerty.pannes.model;

public enum Categorie {
    INFORMATIQUE("Informatique"),
    AUDIOVISUEL("Audiovisuel"),
    ELECTRICITE("Électricité"),
    CLIMATISATION("Climatisation"),
    RESEAU("Réseau"),
    MOBILIER("Mobilier"),
    AUTRE("Autre");

    private final String libelle;

    Categorie(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
