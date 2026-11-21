package com.qwerty.pannes.web;

import com.qwerty.pannes.model.Role;
import com.qwerty.pannes.model.Utilisateur;
import com.qwerty.pannes.repository.UtilisateurRepository;
import com.qwerty.pannes.service.ProfilService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_KEY = "utilisateurId";

    public record LoginRequest(
            @NotBlank(message = "L'e-mail est obligatoire") String email,
            @NotBlank(message = "Le mot de passe est obligatoire") String motDePasse) {}


    public record UtilisateurDto(Long id, String nomComplet, String email, Role role, String photoUrl) {
        static UtilisateurDto de(Utilisateur u, String photoUrl) {
            return new UtilisateurDto(u.getId(), u.getNomComplet(), u.getEmail(), u.getRole(), photoUrl);
        }
    }

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder encoder;
    private final ProfilService profil;

    public AuthController(UtilisateurRepository utilisateurs, PasswordEncoder encoder, ProfilService profil) {
        this.utilisateurs = utilisateurs;
        this.encoder = encoder;
        this.profil = profil;
    }

    @PostMapping("/login")
    public UtilisateurDto login(@Valid @RequestBody LoginRequest requete, HttpSession session) {
        Utilisateur u = utilisateurs.findByEmail(requete.email().trim().toLowerCase())
                .filter(x -> encoder.matches(requete.motDePasse(), x.getMotDePasse()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "E-mail ou mot de passe incorrect."));
        session.setAttribute(SESSION_KEY, u.getId());
        return UtilisateurDto.de(u, profil.photoUrl(u.getId()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }


    @GetMapping("/me")
    public UtilisateurDto me(HttpSession session) {
        Object id = session.getAttribute(SESSION_KEY);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non connecté.");
        }
        return utilisateurs.findById((Long) id)
                .map(u -> UtilisateurDto.de(u, profil.photoUrl(u.getId())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non connecté."));
    }
}