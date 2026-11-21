package com.qwerty.pannes.web;

import com.qwerty.pannes.model.Role;
import jakarta.validation.constraints.NotNull;

public record AdminModificationRequest(
        @NotNull(message = "Choisissez un rôle.")
        Role role,

        Long campusId,

        Long uniteId) {
}