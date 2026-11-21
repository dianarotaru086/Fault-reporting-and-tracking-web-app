package com.qwerty.pannes.web;

import com.qwerty.pannes.model.Role;

import java.time.LocalDateTime;

public record AdminUtilisateurDto(
        Long id,
        String nom,
        String prenom,
        String email,
        Role role,
        String roleLibelle,
        Long campusId,
        String campus,
        Long uniteId,
        String unite,
        LocalDateTime dateInscription,
        long nbSignalees,
        long nbPrises) {
}