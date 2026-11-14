package com.qwerty.pannes.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "panne")
public class Panne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 50)
    private String lieu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categorie categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priorite priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut;

    @Column(name = "date_signalement", nullable = false)
    private LocalDateTime dateSignalement;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    /** L'utilisateur qui a signalé la panne (obligatoire). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signaleur_id", nullable = false)
    private Utilisateur signaleur;

    /** Le responsable qui traite la panne : vide tant que la panne n'est pas prise en charge. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    /**
     * Verrouillage optimiste : si deux responsables valident la prise en charge
     * exactement au même moment, le second échoue au lieu d'écraser le premier.
     */
    @Version
    private Long version;

    protected Panne() {
        // requis par JPA
    }

    public Panne(String titre, String description, String lieu,
                 Categorie categorie, Priorite priorite, Utilisateur signaleur) {
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.categorie = categorie;
        this.priorite = priorite;
        this.signaleur = signaleur;
        this.statut = Statut.NEW;
        this.dateSignalement = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }

    public LocalDateTime getDateResolution() { return dateResolution; }
    public void setDateResolution(LocalDateTime dateResolution) { this.dateResolution = dateResolution; }

    public Utilisateur getSignaleur() { return signaleur; }

    public Utilisateur getResponsable() { return responsable; }
    public void setResponsable(Utilisateur responsable) { this.responsable = responsable; }

    public Long getVersion() { return version; }
}
