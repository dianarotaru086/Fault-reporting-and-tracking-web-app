package com.qwerty.pannes.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfilRequest(
        @NotBlank(message = "Le nom est obligatoire.")
        @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères.")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire.")
        @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères.")
        String prenom,

        @NotBlank(message = "L'e-mail est obligatoire.")
        @Email(message = "L'adresse e-mail n'est pas valide.")
        @Size(max = 100, message = "L'e-mail ne peut pas dépasser 100 caractères.")
        String email,

        @Pattern(regexp = "^$|^[0-9+ ().-]{6,20}$", message = "Numéro de téléphone invalide (6 à 20 chiffres ou symboles + - . ( ) ).")
        String telephone,

        Long campusId,

        Long uniteId) {
}