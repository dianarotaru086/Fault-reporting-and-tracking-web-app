package com.qwerty.pannes.web;

import java.util.List;

/** Les pannes de l'utilisateur connecté : celles qu'il a signalées, et (responsable) celles qu'il traite. */
public record MesPannesDto(List<PanneDto> signalees, List<PanneDto> priseEnCharge) {
}