package com.qwerty.pannes.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.OptimisticLockingFailureException;
import com.qwerty.pannes.service.ErreurChamp;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erreurs.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Certains champs sont invalides.", "erreurs", erreurs));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> illisible(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Les données envoyées sont invalides."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> parametre(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Valeur invalide pour le critère « " + ex.getName() + " »."));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> statut(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason() == null ? "Erreur." : ex.getReason()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> conflit(OptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Cette panne vient d'être modifiée par quelqu'un d'autre. Rechargez la page."));
    }

    @ExceptionHandler(ErreurChamp.class)
    public ResponseEntity<Map<String, Object>> champ(ErreurChamp ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Certains champs sont invalides.",
                        "erreurs", Map.of(ex.getChamp(), ex.getMessage())));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> fichierTropGros(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "La photo ne doit pas dépasser 2 Mo.",
                        "erreurs", Map.of("photo", "La photo ne doit pas dépasser 2 Mo.")));
    }
}
