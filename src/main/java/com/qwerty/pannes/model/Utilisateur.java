package com.qwerty.pannes.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription;

    @Column(length = 20)
    private String telephone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unite_id")
    private Unite unite;

    protected Utilisateur() {
        // requis par JPA
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.dateInscription = LocalDateTime.now();
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Campus getCampus() { return campus; }
    public void setCampus(Campus campus) { this.campus = campus; }

    public Unite getUnite() { return unite; }
    public void setUnite(Unite unite) { this.unite = unite; }
}
