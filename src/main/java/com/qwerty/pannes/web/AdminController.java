package com.qwerty.pannes.web;

import com.qwerty.pannes.service.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminUtilisateurDto> lister(HttpSession session) {
        return service.lister(Connexion.exiger(session));
    }

    @PutMapping("/{id}")
    public AdminUtilisateurDto modifier(@PathVariable Long id,
                                        @Valid @RequestBody AdminModificationRequest demande,
                                        HttpSession session) {
        return service.modifier(Connexion.exiger(session), id, demande);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, HttpSession session) {
        service.supprimer(Connexion.exiger(session), id);
    }
}