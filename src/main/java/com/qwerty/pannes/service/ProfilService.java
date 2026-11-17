package com.qwerty.pannes.service;

import com.qwerty.pannes.model.PhotoProfil;
import com.qwerty.pannes.model.Role;
import com.qwerty.pannes.model.Utilisateur;
import com.qwerty.pannes.repository.PhotoProfilRepository;
import com.qwerty.pannes.repository.UtilisateurRepository;
import com.qwerty.pannes.web.MotDePasseRequest;
import com.qwerty.pannes.web.ProfilDto;
import com.qwerty.pannes.web.ProfilRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;

import com.qwerty.pannes.model.Campus;
import com.qwerty.pannes.model.Unite;
import com.qwerty.pannes.repository.CampusRepository;
import com.qwerty.pannes.repository.UniteRepository;
import com.qwerty.pannes.web.InscriptionRequest;

/** Le profil d'un utilisateur : informations, photo, mot de passe. */
@Service
public class ProfilService {

    private static final long TAILLE_MAX_PHOTO = 2L * 1024 * 1024; // 2 Mo

    private final UtilisateurRepository utilisateurs;
    private final PhotoProfilRepository photos;
    private final CampusRepository campusRepo;
    private final UniteRepository uniteRepo;
    private final PasswordEncoder encoder;

    public ProfilService(UtilisateurRepository utilisateurs, PhotoProfilRepository photos,
                         CampusRepository campusRepo, UniteRepository uniteRepo,
                         PasswordEncoder encoder) {
        this.utilisateurs = utilisateurs;
        this.photos = photos;
        this.campusRepo = campusRepo;
        this.uniteRepo = uniteRepo;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public ProfilDto profil(Long utilisateurId) {
        return versDto(connecte(utilisateurId));
    }

    /**
     * Créer un compte. Le rôle est toujours UTILISATEUR : on ne se donne pas soi-même les droits de responsable.
     * Le serveur revérifie que la faculté appartient bien au campus choisi.
     */
    @Transactional
    public Utilisateur inscrire(InscriptionRequest demande) {
        String email = demande.email().trim().toLowerCase();

        if (utilisateurs.existsByEmail(email)) {
            throw new ErreurChamp("email", "Un compte existe déjà avec cette adresse e-mail.");
        }

        Campus campus = campusRepo.findById(demande.campusId())
                .orElseThrow(() -> new ErreurChamp("campusId", "Campus inconnu."));
        Unite unite = uniteRepo.findById(demande.uniteId())
                .orElseThrow(() -> new ErreurChamp("uniteId", "Faculté ou service inconnu."));
        if (!unite.getCampus().getId().equals(campus.getId())) {
            throw new ErreurChamp("uniteId", "Cette faculté ne se trouve pas sur le campus choisi.");
        }

        if (!demande.motDePasse().equals(demande.confirmation())) {
            throw new ErreurChamp("confirmation", "La confirmation ne correspond pas au mot de passe.");
        }

        Utilisateur utilisateur = new Utilisateur(
                demande.nom().trim(),
                demande.prenom().trim(),
                email,
                encoder.encode(demande.motDePasse()),
                Role.UTILISATEUR);
        utilisateur.setCampus(campus);
        utilisateur.setUnite(unite);
        return utilisateurs.save(utilisateur);
    }

    /** Modifier nom, prénom, e-mail et téléphone. Le rôle ne se modifie pas ici. */
    @Transactional
    /**
     * Modifier nom, prénom, e-mail, téléphone et, pour un utilisateur, son campus et sa faculté.
     * Le rôle ne se modifie pas ici. Le campus d'un responsable est attribué par l'administrateur :
     * sinon il pourrait se donner accès aux pannes d'un autre campus.
     */
    public ProfilDto modifier(Long utilisateurId, ProfilRequest demande) {
        Utilisateur utilisateur = connecte(utilisateurId);

        // L'e-mail sert à se connecter : il doit rester unique (et en minuscules, comme au login)
        String email = demande.email().trim().toLowerCase();
        utilisateurs.findByEmail(email)
                .filter(autre -> !autre.getId().equals(utilisateur.getId()))
                .ifPresent(autre -> {
                    throw new ErreurChamp("email", "Cette adresse e-mail est déjà utilisée.");
                });

        Unite unite = null;
        if (utilisateur.getRole() == Role.UTILISATEUR) {
            if (demande.campusId() == null) {
                throw new ErreurChamp("campusId", "Choisissez votre campus.");
            }
            if (demande.uniteId() == null) {
                throw new ErreurChamp("uniteId", "Choisissez votre faculté ou service.");
            }
            unite = verifierAffectation(demande.campusId(), demande.uniteId());
        }

        utilisateur.setNom(demande.nom().trim());
        utilisateur.setPrenom(demande.prenom().trim());
        utilisateur.setEmail(email);
        utilisateur.setTelephone(demande.telephone() == null || demande.telephone().isBlank()
                ? null : demande.telephone().trim());
        if (unite != null) {
            utilisateur.setCampus(unite.getCampus());
            utilisateur.setUnite(unite);
        }
        utilisateurs.saveAndFlush(utilisateur);

        return versDto(utilisateur);
    }

    /** Vérifie que le campus et la faculté existent et vont ensemble ; renvoie la faculté. */
    private Unite verifierAffectation(Long campusId, Long uniteId) {
        Campus campus = campusRepo.findById(campusId)
                .orElseThrow(() -> new ErreurChamp("campusId", "Campus inconnu."));
        Unite unite = uniteRepo.findById(uniteId)
                .orElseThrow(() -> new ErreurChamp("uniteId", "Faculté ou service inconnu."));
        if (!unite.getCampus().getId().equals(campus.getId())) {
            throw new ErreurChamp("uniteId", "Cette faculté ne se trouve pas sur le campus choisi.");
        }
        return unite;
    }

    /** Changer le mot de passe : il faut connaître l'ancien, et confirmer le nouveau. */
    @Transactional
    public void changerMotDePasse(Long utilisateurId, MotDePasseRequest demande) {
        Utilisateur utilisateur = connecte(utilisateurId);

        if (!encoder.matches(demande.motDePasseActuel(), utilisateur.getMotDePasse())) {
            throw new ErreurChamp("motDePasseActuel", "Le mot de passe actuel est incorrect.");
        }
        if (!demande.nouveauMotDePasse().equals(demande.confirmation())) {
            throw new ErreurChamp("confirmation", "La confirmation ne correspond pas au nouveau mot de passe.");
        }
        if (demande.nouveauMotDePasse().equals(demande.motDePasseActuel())) {
            throw new ErreurChamp("nouveauMotDePasse", "Le nouveau mot de passe doit être différent de l'ancien.");
        }

        utilisateur.setMotDePasse(encoder.encode(demande.nouveauMotDePasse()));
        utilisateurs.save(utilisateur);
    }

    /**
     * Remplacer la photo de profil. On vérifie la taille et le VRAI format de l'image
     * (les premiers octets du fichier), pas seulement son nom ou le type annoncé par le navigateur.
     */
    @Transactional
    public ProfilDto changerPhoto(Long utilisateurId, MultipartFile fichier) {
        Utilisateur utilisateur = connecte(utilisateurId);

        if (fichier == null || fichier.isEmpty()) {
            throw new ErreurChamp("photo", "Choisissez une image.");
        }
        if (fichier.getSize() > TAILLE_MAX_PHOTO) {
            throw new ErreurChamp("photo", "La photo ne doit pas dépasser 2 Mo.");
        }

        byte[] octets;
        try {
            octets = fichier.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de lire le fichier.");
        }

        String type = detecterType(octets);
        if (type == null) {
            throw new ErreurChamp("photo", "Format non accepté : choisissez une image JPEG ou PNG.");
        }

        photos.save(new PhotoProfil(utilisateur.getId(), type, octets, LocalDateTime.now()));
        return versDto(utilisateur);
    }

    @Transactional(readOnly = true)
    public PhotoProfil photo(Long utilisateurId) {
        return photos.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune photo."));
    }

    /**
     * L'adresse de la photo, ou null s'il n'y en a pas. Le "?v=" change à chaque nouvelle photo :
     * le navigateur ne montre donc jamais l'ancienne version gardée en cache.
     */
    public String photoUrl(Long utilisateurId) {
        return photos.dateMiseAJour(utilisateurId)
                .map(date -> "/api/profil/photo?v=" + date.toString().replaceAll("[^0-9]", ""))
                .orElse(null);
    }

    // ------------------------------------------------------------------

    private Utilisateur connecte(Long id) {
        return utilisateurs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté."));
    }

    private ProfilDto versDto(Utilisateur u) {
        return new ProfilDto(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getTelephone(),
                u.getRole(), libelleRole(u.getRole()), u.getDateInscription(), photoUrl(u.getId()),
                u.getCampus() == null ? null : u.getCampus().getNom(),
                u.getUnite() == null ? null : u.getUnite().getNom(),
                u.getCampus() == null ? null : u.getCampus().getId(),
                u.getUnite() == null ? null : u.getUnite().getId());
    }

    private static String libelleRole(Role role) {
        return role == Role.RESPONSABLE ? "Responsable technique" : "Utilisateur";
    }

    /** JPEG commence par FF D8 FF ; PNG par 89 50 4E 47. Autre chose : refusé. */
    private static String detecterType(byte[] o) {
        if (o.length > 4 && (o[0] & 0xFF) == 0xFF && (o[1] & 0xFF) == 0xD8 && (o[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (o.length > 4 && (o[0] & 0xFF) == 0x89 && o[1] == 0x50 && o[2] == 0x4E && o[3] == 0x47) {
            return "image/png";
        }
        return null;
    }
}