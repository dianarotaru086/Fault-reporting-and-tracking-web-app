package com.qwerty.pannes.config;

import com.qwerty.pannes.model.*;
import com.qwerty.pannes.repository.InterventionRepository;
import com.qwerty.pannes.repository.PanneRepository;
import com.qwerty.pannes.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Charge des données d'exemple au premier démarrage (base vide).
 * Indispensable pour la séance de tests de la semaine 11 et pour la soutenance.
 *
 * Comptes de démonstration (mot de passe pour tous : demo123) :
 *   maria.ionescu@exemple.fr    - UTILISATEUR
 *   andrei.popescu@exemple.fr   - UTILISATEUR
 *   ion.georgescu@exemple.fr    - RESPONSABLE
 *   elena.dumitru@exemple.fr    - RESPONSABLE
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String MOT_DE_PASSE_DEMO = "demo123";

    private final UtilisateurRepository utilisateurs;
    private final PanneRepository pannes;
    private final InterventionRepository interventions;
    private final PasswordEncoder encoder;

    public DataInitializer(UtilisateurRepository utilisateurs, PanneRepository pannes,
                           InterventionRepository interventions, PasswordEncoder encoder) {
        this.utilisateurs = utilisateurs;
        this.pannes = pannes;
        this.interventions = interventions;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (utilisateurs.count() > 0) {
            log.info("Données déjà présentes : chargement des exemples ignoré.");
            return;
        }

        String mdp = encoder.encode(MOT_DE_PASSE_DEMO);
        Utilisateur maria  = utilisateurs.save(new Utilisateur("Ionescu", "Maria", "maria.ionescu@exemple.fr", mdp, Role.UTILISATEUR));
        Utilisateur andrei = utilisateurs.save(new Utilisateur("Popescu", "Andrei", "andrei.popescu@exemple.fr", mdp, Role.UTILISATEUR));
        Utilisateur ion    = utilisateurs.save(new Utilisateur("Georgescu", "Ion", "ion.georgescu@exemple.fr", mdp, Role.RESPONSABLE));
        Utilisateur elena  = utilisateurs.save(new Utilisateur("Dumitru", "Elena", "elena.dumitru@exemple.fr", mdp, Role.RESPONSABLE));

        LocalDateTime maintenant = LocalDateTime.now();

        // --- Pannes au statut New ---
        nouvelle("Projecteur ne fonctionne pas", "Le projecteur ne s'allume pas et affiche un écran noir.",
                "C1006", Categorie.AUDIOVISUEL, Priorite.HAUTE, maria, maintenant.minusDays(1));
        nouvelle("Prise électrique hors service", "La prise murale près du bureau du professeur ne fournit plus de courant.",
                "B204", Categorie.ELECTRICITE, Priorite.MOYENNE, andrei, maintenant.minusDays(2));
        nouvelle("Lumière qui clignote", "Le néon du fond de la salle clignote en permanence.",
                "A1012", Categorie.ELECTRICITE, Priorite.BASSE, maria, maintenant.minusDays(3));
        nouvelle("Imprimante bloquée", "L'imprimante du couloir affiche un bourrage papier permanent.",
                "C2010", Categorie.INFORMATIQUE, Priorite.MOYENNE, andrei, maintenant.minusHours(5));

        // --- Pannes In progress ---
        enCours("Climatisation défaillante", "La climatisation souffle de l'air chaud.",
                "C3205", Categorie.CLIMATISATION, Priorite.MOYENNE, maria, ion, maintenant.minusDays(4));
        enCours("Table endommagée", "Un pied de table est cassé, la table est instable.",
                "C3201", Categorie.MOBILIER, Priorite.BASSE, andrei, elena, maintenant.minusDays(6));

        // --- Pannes Done ---
        resolue("Ordinateur ne s'allume pas", "Le poste n°12 ne démarre plus.",
                "C3201", Categorie.INFORMATIQUE, Priorite.HAUTE, maria, ion, maintenant.minusDays(10));
        resolue("Accès Wi-Fi impossible", "Impossible de se connecter au réseau Wi-Fi dans l'amphithéâtre.",
                "A1024", Categorie.RESEAU, Priorite.MOYENNE, andrei, elena, maintenant.minusDays(12));

        log.info("Données d'exemple chargées : {} utilisateurs, {} pannes, {} interventions.",
                utilisateurs.count(), pannes.count(), interventions.count());
    }

    private Panne nouvelle(String titre, String description, String lieu, Categorie categorie,
                           Priorite priorite, Utilisateur signaleur, LocalDateTime date) {
        Panne p = new Panne(titre, description, lieu, categorie, priorite, signaleur);
        p.setDateSignalement(date);
        p = pannes.save(p);
        interventions.save(new Intervention(p, signaleur, TypeAction.SIGNALEMENT, null, date));
        return p;
    }

    private Panne enCours(String titre, String description, String lieu, Categorie categorie,
                          Priorite priorite, Utilisateur signaleur, Utilisateur responsable, LocalDateTime date) {
        Panne p = nouvelle(titre, description, lieu, categorie, priorite, signaleur, date);
        p.setResponsable(responsable);
        p.setStatut(Statut.IN_PROGRESS);
        p = pannes.save(p);
        interventions.save(new Intervention(p, responsable, TypeAction.PRISE_EN_CHARGE, null, date.plusHours(2)));
        return p;
    }

    private Panne resolue(String titre, String description, String lieu, Categorie categorie,
                          Priorite priorite, Utilisateur signaleur, Utilisateur responsable, LocalDateTime date) {
        Panne p = enCours(titre, description, lieu, categorie, priorite, signaleur, responsable, date);
        LocalDateTime dateResolution = date.plusDays(1);
        p.setStatut(Statut.DONE);
        p.setDateResolution(dateResolution);
        p = pannes.save(p);
        interventions.save(new Intervention(p, responsable, TypeAction.RESOLUTION,
                "Intervention terminée, problème résolu.", dateResolution));
        return p;
    }
}
