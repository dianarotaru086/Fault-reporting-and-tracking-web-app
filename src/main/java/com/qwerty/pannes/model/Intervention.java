package com.qwerty.pannes.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Un événement dans l'historique d'une panne : signalement, prise en charge,
 * commentaire du responsable ou résolution.
 */
@Entity
@Table(name = "intervention")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "panne_id", nullable = false)
    private Panne panne;

    /** Qui a fait l'action (le signaleur pour SIGNALEMENT, sinon le responsable). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeAction action;

    @Column(length = 1000)
    private String commentaire;

    @Column(name = "date_intervention", nullable = false)
    private LocalDateTime dateIntervention;

    protected Intervention() {
        // requis par JPA
    }

    public Intervention(Panne panne, Utilisateur auteur, TypeAction action,
                        String commentaire, LocalDateTime dateIntervention) {
        this.panne = panne;
        this.auteur = auteur;
        this.action = action;
        this.commentaire = commentaire;
        this.dateIntervention = dateIntervention;
    }

    public Long getId() { return id; }
    public Panne getPanne() { return panne; }
    public Utilisateur getAuteur() { return auteur; }
    public TypeAction getAction() { return action; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getDateIntervention() { return dateIntervention; }
}
