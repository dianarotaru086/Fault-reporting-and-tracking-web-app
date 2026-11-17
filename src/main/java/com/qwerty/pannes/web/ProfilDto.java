package com.qwerty.pannes.web;

import com.qwerty.pannes.model.Role;

import java.time.LocalDateTime;

public record ProfilDto(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        Role role,
        String roleLibelle,
        LocalDateTime dateInscription,
        String photoUrl,
        String campus,
        String unite,
        Long campusId,
        Long uniteId) {
}