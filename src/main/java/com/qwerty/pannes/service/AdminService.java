package com.qwerty.pannes.service;

import com.qwerty.pannes.model.Campus;
import com.qwerty.pannes.model.Role;
import com.qwerty.pannes.model.Statut;
import com.qwerty.pannes.model.Unite;
import com.qwerty.pannes.model.Utilisateur;
import com.qwerty.pannes.repository.CampusRepository;
import com.qwerty.pannes.repository.PanneRepository;
import com.qwerty.pannes.repository.PhotoProfilRepository;
import com.qwerty.pannes.repository.UniteRepository;
import com.qwerty.pannes.repository.UtilisateurRepository;
import com.qwerty.pannes.web.AdminModificationRequest;
import com.qwerty.pannes.web.AdminUtilisateurDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

    private final UtilisateurRepository utilisateurs;
    private final PanneRepository pannes;
    private final CampusRepository campusRepo;
    private final UniteRepository uniteRepo;
    private final PhotoProfilRepository photos;

    public AdminService(UtilisateurRepository utilisateurs, PanneRepository pannes,
                        CampusRepository campusRepo, UniteRepository uniteRepo,
                        PhotoProfilRepository photos) {
        this.utilisateurs = utilisateurs;
        this.pannes = pannes;
        this.campusRepo = campusRepo;
        this.uniteRepo = uniteRepo;
        this.photos = photos;
    }

    @Transactional(readOnly = true)
    public List<AdminUtilisateurDto> lister(Long adminId) {
        verifierAdmin(adminId);
        return utilisateurs.findAllByOrderByNomAscPrenomAsc().stream().map(this::versDto).toList();
    }

    @Transactional
    public AdminUtilisateurDto modifier(Long adminId, Long id, AdminModificationRequest demande) {
        verifierAdmin(adminId);
        Utilisateur u = trouver(id);

        if (u.getId().equals(adminId) && demande.role() != u.getRole()) {
            throw new ErreurChamp("role", "Vous ne pouvez pas modifier votre propre rôle.");
        }
        if (u.getRole() == Role.RESPONSABLE && demande.role() != Role.RESPONSABLE) {
            long enCours = pannes.countByResponsableIdAndStatut(u.getId(), Statut.IN_PROGRESS);
            if (enCours > 0) {
                throw new ErreurChamp("role", "Ce responsable traite encore " + enCours
                        + " panne(s) en cours : elles doivent d'abord être résolues.");
            }
        }

        switch (demande.role()) {
            case ADMIN -> {
                u.setCampus(null);
                u.setUnite(null);
            }
            case RESPONSABLE -> {
                if (demande.campusId() == null) {
                    throw new ErreurChamp("campusId", "Choisissez le campus du responsable.");
                }
                u.setCampus(campusRepo.findById(demande.campusId())
                        .orElseThrow(() -> new ErreurChamp("campusId", "Campus inconnu.")));
                u.setUnite(null);
            }
            case UTILISATEUR -> {
                if (demande.campusId() == null) {
                    throw new ErreurChamp("campusId", "Choisissez le campus.");
                }
                if (demande.uniteId() == null) {
                    throw new ErreurChamp("uniteId", "Choisissez la faculté ou le service.");
                }
                Unite unite = verifierAffectation(demande.campusId(), demande.uniteId());
                u.setCampus(unite.getCampus());
                u.setUnite(unite);
            }
        }
        u.setRole(demande.role());
        utilisateurs.saveAndFlush(u);

        return versDto(u);
    }

    @Transactional
    public void supprimer(Long adminId, Long id) {
        verifierAdmin(adminId);
        Utilisateur u = trouver(id);

        if (u.getId().equals(adminId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vous ne pouvez pas supprimer votre propre compte.");
        }

        long signalees = pannes.countBySignaleurId(id);
        long prises = pannes.countByResponsableId(id);
        if (signalees + prises > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Impossible de supprimer " + u.getNomComplet() + " : ce compte est lié à "
                            + (signalees + prises) + " panne(s) (" + signalees + " signalée(s), "
                            + prises + " prise(s) en charge). Supprimez d'abord ces pannes.");
        }

        photos.deleteById(id);
        utilisateurs.delete(u);
    }


    private void verifierAdmin(Long id) {
        Utilisateur u = utilisateurs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté."));
        if (u.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Réservé aux administrateurs.");
        }
    }

    private Utilisateur trouver(Long id) {
        return utilisateurs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte introuvable."));
    }

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

    private AdminUtilisateurDto versDto(Utilisateur u) {
        return new AdminUtilisateurDto(
                u.getId(), u.getNom(), u.getPrenom(), u.getEmail(),
                u.getRole(), libelleRole(u.getRole()),
                u.getCampus() == null ? null : u.getCampus().getId(),
                u.getCampus() == null ? null : u.getCampus().getNom(),
                u.getUnite() == null ? null : u.getUnite().getId(),
                u.getUnite() == null ? null : u.getUnite().getNom(),
                u.getDateInscription(),
                pannes.countBySignaleurId(u.getId()),
                pannes.countByResponsableId(u.getId()));
    }

    private static String libelleRole(Role role) {
        return switch (role) {
            case RESPONSABLE -> "Responsable technique";
            case ADMIN -> "Administrateur";
            case UTILISATEUR -> "Utilisateur";
        };
    }
}