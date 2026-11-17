package com.qwerty.pannes.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InscriptionRequest(
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

        @NotNull(message = "Choisissez votre campus.")
        Long campusId,

        @NotNull(message = "Choisissez votre faculté ou service.")
        Long uniteId,

        @NotBlank(message = "Choisissez un mot de passe.")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
        String motDePasse,

        @NotBlank(message = "Confirmez le mot de passe.")
        String confirmation) {
}