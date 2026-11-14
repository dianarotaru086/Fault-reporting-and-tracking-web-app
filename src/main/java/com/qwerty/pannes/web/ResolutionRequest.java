package com.qwerty.pannes.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolutionRequest(
        @NotBlank(message = "Décrivez ce qui a été fait pour résoudre la panne.")
        @Size(min = 5, max = 1000, message = "Le commentaire doit contenir entre 5 et 1000 caractères.")
        String commentaire) {
}