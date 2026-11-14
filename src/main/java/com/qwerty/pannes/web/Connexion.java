package com.qwerty.pannes.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Petit utilitaire : retrouver l'utilisateur connecté à partir de la session. */
public final class Connexion {

    private Connexion() {
    }

    /** Renvoie l'id de l'utilisateur connecté, ou refuse la requête (401). */
    public static Long exiger(HttpSession session) {
        Object id = session.getAttribute(AuthController.SESSION_KEY);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté.");
        }
        return (Long) id;
    }
}
