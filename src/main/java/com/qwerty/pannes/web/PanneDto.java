package com.qwerty.pannes.web;

import com.qwerty.pannes.model.*;

import java.time.LocalDateTime;

/** Ce que le serveur renvoie au navigateur pour décrire une panne. */
public record PanneDto(
        Long id,
        String titre,
        String description,
        String lieu,
        Categorie categorie,
        String categorieLibelle,
        Priorite priorite,
        String prioriteLibelle,
        Statut statut,
        String statutLibelle,
        LocalDateTime dateSignalement,
        LocalDateTime dateResolution,
        String signaleur,
        String responsable) {

    /** À appeler pendant une transaction : signaleur et responsable sont chargés à la demande. */
    public static PanneDto de(Panne p) {
        return new PanneDto(
                p.getId(), p.getTitre(), p.getDescription(), p.getLieu(),
                p.getCategorie(), p.getCategorie().getLibelle(),
                p.getPriorite(), p.getPriorite().getLibelle(),
                p.getStatut(), p.getStatut().getLibelle(),
                p.getDateSignalement(), p.getDateResolution(),
                p.getSignaleur().getNomComplet(),
                p.getResponsable() == null ? null : p.getResponsable().getNomComplet());
    }
}
