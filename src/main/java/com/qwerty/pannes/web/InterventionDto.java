package com.qwerty.pannes.web;

import com.qwerty.pannes.model.Intervention;
import com.qwerty.pannes.model.TypeAction;

import java.time.LocalDateTime;

/** Une ligne de l'historique d'une panne. */
public record InterventionDto(
        Long id,
        TypeAction action,
        String actionLibelle,
        String auteur,
        String commentaire,
        LocalDateTime date) {

    public static InterventionDto de(Intervention i) {
        return new InterventionDto(i.getId(), i.getAction(), libelle(i.getAction()),
                i.getAuteur().getNomComplet(), i.getCommentaire(), i.getDateIntervention());
    }

    private static String libelle(TypeAction action) {
        return switch (action) {
            case SIGNALEMENT -> "Signalement";
            case MODIFICATION -> "Modification";
            case PRISE_EN_CHARGE -> "Prise en charge";
            case COMMENTAIRE -> "Commentaire";
            case RESOLUTION -> "Résolution";
        };
    }
}