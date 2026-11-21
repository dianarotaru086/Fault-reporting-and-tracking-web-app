package com.qwerty.pannes.service;

import com.qwerty.pannes.model.*;
import com.qwerty.pannes.repository.InterventionRepository;
import com.qwerty.pannes.repository.PanneRepository;
import com.qwerty.pannes.repository.UniteRepository;
import com.qwerty.pannes.repository.UtilisateurRepository;
import com.qwerty.pannes.web.InterventionDto;
import com.qwerty.pannes.web.MesPannesDto;
import com.qwerty.pannes.web.PanneDetailDto;
import com.qwerty.pannes.web.PanneDto;
import com.qwerty.pannes.web.PanneRequest;
import com.qwerty.pannes.web.ResolutionRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


@Service
public class PanneService {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final PanneRepository pannes;
    private final UtilisateurRepository utilisateurs;
    private final InterventionRepository interventions;
    private final UniteRepository unites;

    public PanneService(PanneRepository pannes, UtilisateurRepository utilisateurs,
                        InterventionRepository interventions, UniteRepository unites) {
        this.pannes = pannes;
        this.utilisateurs = utilisateurs;
        this.interventions = interventions;
        this.unites = unites;
    }
    @Transactional
    public PanneDto signaler(PanneRequest demande, Long utilisateurId) {
        Utilisateur signaleur = connecte(utilisateurId);

        Panne panne = new Panne(
                demande.titre().trim(),
                demande.description().trim(),
                demande.lieu().trim(),
                demande.categorie(),
                demande.priorite(),
                signaleur);
        panne.setUnite(trouverUnite(demande.uniteId()));
        panne = pannes.save(panne);

        interventions.save(new Intervention(
                panne, signaleur, TypeAction.SIGNALEMENT, null, panne.getDateSignalement()));

        return PanneDto.de(panne);
    }

    @Transactional(readOnly = true)
    public List<PanneDto> lister(String recherche, Long campusId, Long uniteId,
                                 Categorie categorie, Statut statut, Priorite priorite) {
        Specification<Panne> spec = (root, query, cb) -> cb.conjunction();

        if (campusId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("unite").get("campus").get("id"), campusId));
        }
        if (uniteId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("unite").get("id"), uniteId));
        }
        if (categorie != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categorie"), categorie));
        }
        if (statut != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statut"), statut));
        }
        if (priorite != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priorite"), priorite));
        }

        String cherche = normaliser(recherche);

        return pannes.findAll(spec, Sort.by(Sort.Order.desc("dateSignalement"), Sort.Order.desc("id")))
                .stream()
                .filter(p -> cherche.isEmpty() || correspond(p, cherche))
                .map(PanneDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PanneDetailDto detail(Long panneId, Long utilisateurId) {
        return construireDetail(trouver(panneId), connecte(utilisateurId));
    }

    @Transactional
    public PanneDetailDto prendreEnCharge(Long panneId, Long utilisateurId) {
        Utilisateur responsable = connecte(utilisateurId);
        if (responsable.getRole() != Role.RESPONSABLE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul un responsable technique peut prendre une panne en charge.");
        }

        Panne panne = trouver(panneId);

        if (!memeCampus(responsable, panne)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageCampus(responsable, panne));
        }
        if (panne.getStatut() == Statut.DONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette panne est déjà résolue.");
        }
        if (panne.getStatut() != Statut.NEW) {
            String qui = panne.getResponsable() != null
                    ? panne.getResponsable().getNomComplet() : "un autre responsable";
            String depuis = interventions.findByPanneIdOrderByDateInterventionAsc(panne.getId()).stream()
                    .filter(i -> i.getAction() == TypeAction.PRISE_EN_CHARGE)
                    .map(Intervention::getDateIntervention)
                    .reduce((premier, dernier) -> dernier)
                    .map(d -> " depuis le " + d.format(FORMAT_DATE))
                    .orElse("");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette panne est déjà prise en charge par " + qui + depuis + ".");
        }

        panne.setResponsable(responsable);
        panne.setStatut(Statut.IN_PROGRESS);
        pannes.saveAndFlush(panne);
        interventions.save(new Intervention(panne, responsable, TypeAction.PRISE_EN_CHARGE,
                null, LocalDateTime.now()));

        return construireDetail(panne, responsable);
    }


    @Transactional
    public PanneDetailDto resoudre(Long panneId, Long utilisateurId, ResolutionRequest demande) {
        Utilisateur utilisateur = connecte(utilisateurId);
        Panne panne = trouver(panneId);

        if (panne.getStatut() == Statut.DONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette panne est déjà résolue.");
        }
        if (panne.getStatut() == Statut.NEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette panne doit d'abord être prise en charge.");
        }
        if (panne.getResponsable() == null || !panne.getResponsable().getId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul le responsable qui a pris cette panne en charge peut la marquer comme résolue.");
        }

        LocalDateTime maintenant = LocalDateTime.now();
        panne.setStatut(Statut.DONE);
        panne.setDateResolution(maintenant);
        pannes.saveAndFlush(panne);

        interventions.save(new Intervention(panne, utilisateur, TypeAction.RESOLUTION,
                demande.commentaire().trim(), maintenant));

        return construireDetail(panne, utilisateur);
    }

    @Transactional(readOnly = true)
    public MesPannesDto mesPannes(Long utilisateurId) {
        Utilisateur utilisateur = connecte(utilisateurId);

        List<PanneDto> signalees = pannes.findBySignaleurIdOrderByDateSignalementDesc(utilisateur.getId())
                .stream().map(PanneDto::de).toList();

        List<PanneDto> priseEnCharge = utilisateur.getRole() == Role.RESPONSABLE
                ? pannes.findByResponsableIdOrderByDateSignalementDesc(utilisateur.getId())
                .stream().map(PanneDto::de).toList()
                : List.of();

        return new MesPannesDto(signalees, priseEnCharge);
    }


    @Transactional
    public PanneDto modifier(Long panneId, PanneRequest demande, Long utilisateurId) {
        Utilisateur utilisateur = connecte(utilisateurId);
        Panne panne = trouverModifiable(panneId, utilisateur);

        panne.setTitre(demande.titre().trim());
        panne.setDescription(demande.description().trim());
        panne.setUnite(trouverUnite(demande.uniteId()));
        panne.setLieu(demande.lieu().trim());
        panne.setCategorie(demande.categorie());
        panne.setPriorite(demande.priorite());
        pannes.saveAndFlush(panne);

        interventions.save(new Intervention(panne, utilisateur, TypeAction.MODIFICATION,
                null, LocalDateTime.now()));

        return PanneDto.de(panne);
    }

    @Transactional
    public void supprimer(Long panneId, Long utilisateurId) {
        Utilisateur utilisateur = connecte(utilisateurId);
        Panne panne = utilisateur.getRole() == Role.ADMIN
                ? trouver(panneId)
                : trouverModifiable(panneId, utilisateur);

        interventions.deleteByPanneId(panne.getId());
        pannes.delete(panne);
    }

    private Utilisateur connecte(Long id) {
        return utilisateurs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté."));
    }

    private Panne trouver(Long id) {
        return pannes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Panne introuvable."));
    }

    private Unite trouverUnite(Long id) {
        return unites.findById(id)
                .orElseThrow(() -> new ErreurChamp("uniteId", "Faculté ou service inconnu."));
    }

    private Panne trouverModifiable(Long panneId, Utilisateur utilisateur) {
        Panne panne = trouver(panneId);

        if (!panne.getSignaleur().getId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seule la personne qui a signalé cette panne peut la modifier ou la supprimer.");
        }
        if (panne.getStatut() != Statut.NEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette panne ne peut plus être modifiée : elle est déjà prise en charge.");
        }
        return panne;
    }

    private static Campus campusDe(Panne panne) {
        return panne.getUnite() == null ? null : panne.getUnite().getCampus();
    }

    private static boolean memeCampus(Utilisateur responsable, Panne panne) {
        Campus campusPanne = campusDe(panne);
        if (campusPanne == null) {
            return true;   // ancienne panne sans campus : pas de restriction
        }
        return responsable.getCampus() != null
                && responsable.getCampus().getId().equals(campusPanne.getId());
    }

    private static String nomCourt(Campus campus) {
        return campus.getNom().replaceAll("\\s*\\(.*?\\)", "");
    }

    private static String messageCampus(Utilisateur responsable, Panne panne) {
        String sien = responsable.getCampus() == null
                ? "Votre compte n'est rattaché à aucun campus."
                : "Vous êtes responsable du campus " + nomCourt(responsable.getCampus()) + ".";
        return "Cette panne relève du campus " + nomCourt(campusDe(panne)) + ". " + sien;
    }

    private PanneDetailDto construireDetail(Panne panne, Utilisateur utilisateur) {
        List<InterventionDto> historique = interventions
                .findByPanneIdOrderByDateInterventionAsc(panne.getId()).stream()
                .map(InterventionDto::de)
                .toList();

        boolean estResponsableTechnique = utilisateur.getRole() == Role.RESPONSABLE;
        boolean estAdmin = utilisateur.getRole() == Role.ADMIN;
        boolean estLeResponsableDeLaPanne = panne.getResponsable() != null
                && panne.getResponsable().getId().equals(utilisateur.getId());
        boolean estLeSignaleur = panne.getSignaleur().getId().equals(utilisateur.getId());
        boolean bonCampus = memeCampus(utilisateur, panne);

        String avertissement = estResponsableTechnique && panne.getStatut() == Statut.NEW && !bonCampus
                ? messageCampus(utilisateur, panne) : null;

        return new PanneDetailDto(
                PanneDto.de(panne),
                historique,
                estResponsableTechnique && panne.getStatut() == Statut.NEW && bonCampus,
                estLeResponsableDeLaPanne && panne.getStatut() == Statut.IN_PROGRESS,
                estLeSignaleur && panne.getStatut() == Statut.NEW,
                (estLeSignaleur && panne.getStatut() == Statut.NEW) || estAdmin,
                avertissement);
    }

    private static boolean correspond(Panne p, String cherche) {
        return normaliser(p.getTitre()).contains(cherche)
                || normaliser(p.getDescription()).contains(cherche)
                || normaliser(p.getLieu()).contains(cherche)
                || normaliser(p.getCategorie().getLibelle()).contains(cherche);
    }

    private static String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")              // supprime les accents
                .replaceAll("[^\\p{L}\\p{N}\\s]", "")   // supprime la ponctuation
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}