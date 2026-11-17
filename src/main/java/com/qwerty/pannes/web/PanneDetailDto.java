package com.qwerty.pannes.web;

import java.util.List;

/**
 * Une panne avec son historique, et ce que l'utilisateur connecté a le droit d'en faire.
 * Le navigateur s'en sert pour afficher ou non les boutons ; le serveur revérifie toujours.
 * "avertissement" explique pourquoi un responsable ne peut pas prendre la panne (autre campus).
 */
public record PanneDetailDto(
        PanneDto panne,
        List<InterventionDto> interventions,
        boolean peutPrendreEnCharge,
        boolean peutResoudre,
        boolean peutModifier,
        String avertissement) {
}