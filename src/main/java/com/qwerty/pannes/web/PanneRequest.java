package com.qwerty.pannes.web;

import com.qwerty.pannes.model.Categorie;
import com.qwerty.pannes.model.Priorite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record PanneRequest(
        @NotBlank(message = "Le titre est obligatoire.")
        @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères.")
        String titre,

        @NotBlank(message = "La description est obligatoire.")
        @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères.")
        String description,

        @NotNull(message = "Choisissez la faculté ou le service concerné.")
        Long uniteId,

        @NotBlank(message = "Le lieu est obligatoire.")
        @Size(max = 50, message = "Le lieu ne peut pas dépasser 50 caractères.")
        String lieu,

        @NotNull(message = "Choisissez une catégorie.")
        Categorie categorie,

        @NotNull(message = "Choisissez une priorité.")
        Priorite priorite) {
}