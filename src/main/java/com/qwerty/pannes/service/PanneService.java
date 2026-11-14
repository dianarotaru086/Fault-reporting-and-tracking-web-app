package com.qwerty.pannes.service;

import com.qwerty.pannes.model.*;
import com.qwerty.pannes.repository.InterventionRepository;
import com.qwerty.pannes.repository.PanneRepository;
import com.qwerty.pannes.repository.UtilisateurRepository;
import com.qwerty.pannes.web.InterventionDto;
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

/** La logique métier des pannes. Le contrôleur reste fin, les règles sont ici. */
@Service
public class PanneService {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final PanneRepository pannes;
    private final UtilisateurRepository utilisateurs;
    private final InterventionRepository interventions;

    public PanneService(PanneRepository pannes, UtilisateurRepository utilisateurs,
                        InterventionRepository interventions) {
        this.pannes = pannes;
        this.utilisateurs = utilisateurs;
        this.interventions = interventions;
    }

    // ------------------------------------------------------------------
    // Signaler une panne
    // ------------------------------------------------------------------

    /**
     * Enregistre une nouvelle panne au statut New et note le signalement dans l'historique.
     * @Transactional : les deux enregistrements réussissent ensemble, ou aucun.
     */
    @Transactional
    public PanneDto signaler(PanneRequest demande, Long utilisateurId) {
        Utilisateur signaleur = connecte(utilisateurId);

        Panne panne = pannes.save(new Panne(
                demande.titre().trim(),
                demande.description().trim(),
                demande.lieu().trim(),
                demande.categorie(),
                demande.priorite(),
                signaleur));

        interventions.save(new Intervention(
                panne, signaleur, TypeAction.SIGNALEMENT, null, panne.getDateSignalement()));

        return PanneDto.de(panne);
    }

    // ------------------------------------------------------------------
    // Liste, recherche et filtres
    // ------------------------------------------------------------------

    /**
     * Liste les pannes, de la plus récente à la plus ancienne, en combinant les critères.
     * Un critère absent (null) est simplement ignoré. Les critères présents s'ajoutent (ET).
     *
     * Les filtres catégorie / statut / priorité sont appliqués par la base de données.
     * La recherche texte est appliquée ensuite en Java : elle ignore majuscules, accents
     * et ponctuation (« electricite » trouve « Électricité », « wifi » trouve « Wi-Fi »),
     * quel que soit le réglage de MySQL.
     *
     * @param recherche texte cherché dans le titre, la description, le lieu et la catégorie
     */
    @Transactional(readOnly = true)
    public List<PanneDto> lister(String recherche, Categorie categorie, Statut statut, Priorite priorite) {
        Specification<Panne> spec = (root, query, cb) -> cb.conjunction();

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

        return pannes.findAll(spec, Sort.by(Sort.Direction.DESC, "dateSignalement"))
                .stream()
                .filter(p -> cherche.isEmpty() || correspond(p, cherche))
                .map(PanneDto::de)
                .toList();
    }

    // ------------------------------------------------------------------
    // Détail, prise en charge, résolution
    // ------------------------------------------------------------------

    /** Le détail d'une panne, avec son historique et les droits de l'utilisateur connecté. */
    @Transactional(readOnly = true)
    public PanneDetailDto detail(Long panneId, Long utilisateurId) {
        return construireDetail(trouver(panneId), connecte(utilisateurId));
    }

    /**
     * RÈGLE SPÉCIFIQUE : une panne ne peut être prise en charge que si elle est encore New.
     * Si un autre responsable l'a déjà prise, la demande est refusée (409) avec son nom.
     */
    @Transactional
    public PanneDetailDto prendreEnCharge(Long panneId, Long utilisateurId) {
        Utilisateur responsable = connecte(utilisateurId);
        if (responsable.getRole() != Role.RESPONSABLE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul un responsable technique peut prendre une panne en charge.");
        }

        Panne panne = trouver(panneId);

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

    /** Seul le responsable qui a pris la panne en charge peut la marquer comme résolue. */
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

    // ------------------------------------------------------------------
    // Méthodes internes
    // ------------------------------------------------------------------

    private Utilisateur connecte(Long id) {
        return utilisateurs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté."));
    }

    private Panne trouver(Long id) {
        return pannes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Panne introuvable."));
    }

    /** Assemble la panne, son historique et ce que l'utilisateur a le droit d'en faire. */
    private PanneDetailDto construireDetail(Panne panne, Utilisateur utilisateur) {
        List<InterventionDto> historique = interventions
                .findByPanneIdOrderByDateInterventionAsc(panne.getId()).stream()
                .map(InterventionDto::de)
                .toList();

        boolean estResponsableTechnique = utilisateur.getRole() == Role.RESPONSABLE;
        boolean estLeResponsableDeLaPanne = panne.getResponsable() != null
                && panne.getResponsable().getId().equals(utilisateur.getId());
        boolean estLeSignaleur = panne.getSignaleur().getId().equals(utilisateur.getId());

        return new PanneDetailDto(
                PanneDto.de(panne),
                historique,
                estResponsableTechnique && panne.getStatut() == Statut.NEW,
                estLeResponsableDeLaPanne && panne.getStatut() == Statut.IN_PROGRESS,
                estLeSignaleur && panne.getStatut() == Statut.NEW);
    }

    /** Vrai si le texte cherché apparaît dans le titre, la description, le lieu ou la catégorie. */
    private static boolean correspond(Panne p, String cherche) {
        return normaliser(p.getTitre()).contains(cherche)
                || normaliser(p.getDescription()).contains(cherche)
                || normaliser(p.getLieu()).contains(cherche)
                || normaliser(p.getCategorie().getLibelle()).contains(cherche);
    }

    /**
     * Met un texte sous une forme comparable : sans accents, sans ponctuation,
     * en minuscules, avec des espaces simples.
     * Exemple : « Électricité » devient « electricite », « Wi-Fi » devient « wifi ».
     */
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