package com.qwerty.pannes.web;

import com.qwerty.pannes.model.PhotoProfil;
import com.qwerty.pannes.service.ProfilService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/profil")
public class ProfilController {

    private final ProfilService service;

    public ProfilController(ProfilService service) {
        this.service = service;
    }

    @GetMapping
    public ProfilDto profil(HttpSession session) {
        return service.profil(Connexion.exiger(session));
    }

    @PutMapping
    public ProfilDto modifier(@Valid @RequestBody ProfilRequest demande, HttpSession session) {
        return service.modifier(Connexion.exiger(session), demande);
    }

    @PutMapping("/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changerMotDePasse(@Valid @RequestBody MotDePasseRequest demande, HttpSession session) {
        service.changerMotDePasse(Connexion.exiger(session), demande);
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfilDto changerPhoto(@RequestParam("photo") MultipartFile photo, HttpSession session) {
        return service.changerPhoto(Connexion.exiger(session), photo);
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> photo(HttpSession session) {
        PhotoProfil photo = service.photo(Connexion.exiger(session));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getTypeContenu()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePrivate())
                .body(photo.getContenu());
    }
}