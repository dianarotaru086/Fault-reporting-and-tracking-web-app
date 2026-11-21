package com.qwerty.pannes.web;

import com.qwerty.pannes.model.*;

import java.time.LocalDateTime;

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
        String responsable,
        Long campusId,
        String campus,
        String campusAdresse,
        Long uniteId,
        String unite) {

    public static PanneDto de(Panne p) {
        Unite unite = p.getUnite();
        Campus campus = unite == null ? null : unite.getCampus();
        return new PanneDto(
                p.getId(), p.getTitre(), p.getDescription(), p.getLieu(),
                p.getCategorie(), p.getCategorie().getLibelle(),
                p.getPriorite(), p.getPriorite().getLibelle(),
                p.getStatut(), p.getStatut().getLibelle(),
                p.getDateSignalement(), p.getDateResolution(),
                p.getSignaleur().getNomComplet(),
                p.getResponsable() == null ? null : p.getResponsable().getNomComplet(),
                campus == null ? null : campus.getId(),
                campus == null ? null : campus.getNom(),
                campus == null ? null : campus.getAdresse(),
                unite == null ? null : unite.getId(),
                unite == null ? null : unite.getNom());
    }
}