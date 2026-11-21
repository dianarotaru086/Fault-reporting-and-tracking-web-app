package com.qwerty.pannes.web;

import java.util.List;

public record PanneDetailDto(
        PanneDto panne,
        List<InterventionDto> interventions,
        boolean peutPrendreEnCharge,
        boolean peutResoudre,
        boolean peutModifier,
        boolean peutSupprimer,
        String avertissement) {
}