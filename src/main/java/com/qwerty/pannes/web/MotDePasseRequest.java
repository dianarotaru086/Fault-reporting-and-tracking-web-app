package com.qwerty.pannes.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotDePasseRequest(
        @NotBlank(message = "Entrez votre mot de passe actuel.")
        String motDePasseActuel,

        @NotBlank(message = "Entrez le nouveau mot de passe.")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
        String nouveauMotDePasse,

        @NotBlank(message = "Confirmez le nouveau mot de passe.")
        String confirmation) {
}