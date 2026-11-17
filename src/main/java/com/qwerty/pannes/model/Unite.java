package com.qwerty.pannes.model;

import jakarta.persistence.*;

/** Une faculté, ou un service commun (ex. le Rectorat), situé sur un campus. */
@Entity
@Table(name = "unite")
public class Unite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    protected Unite() {
        // requis par JPA
    }

    public Unite(String nom, Campus campus) {
        this.nom = nom;
        this.campus = campus;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public Campus getCampus() { return campus; }
}