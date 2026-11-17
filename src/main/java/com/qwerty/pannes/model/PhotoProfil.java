package com.qwerty.pannes.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** La photo de profil d'un utilisateur, rangée à part pour ne pas alourdir chaque lecture d'utilisateur. */
@Entity
@Table(name = "photo_profil")
public class PhotoProfil {

    @Id
    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "type_contenu", nullable = false, length = 30)
    private String typeContenu;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenu;

    @Column(name = "date_mise_a_jour", nullable = false)
    private LocalDateTime dateMiseAJour;

    protected PhotoProfil() {
        // requis par JPA
    }

    public PhotoProfil(Long utilisateurId, String typeContenu, byte[] contenu, LocalDateTime dateMiseAJour) {
        this.utilisateurId = utilisateurId;
        this.typeContenu = typeContenu;
        this.contenu = contenu;
        this.dateMiseAJour = dateMiseAJour;
    }

    public Long getUtilisateurId() { return utilisateurId; }
    public String getTypeContenu() { return typeContenu; }
    public byte[] getContenu() { return contenu; }
    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
}