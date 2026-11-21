package com.qwerty.pannes.model;

import jakarta.persistence.*;

@Entity
@Table(name = "campus")
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nom;

    @Column(nullable = false, length = 120)
    private String adresse;

    protected Campus() {

    }

    public Campus(String nom, String adresse) {
        this.nom = nom;
        this.adresse = adresse;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getAdresse() { return adresse; }
}