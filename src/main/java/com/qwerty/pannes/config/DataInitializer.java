package com.qwerty.pannes.config;

import com.qwerty.pannes.model.*;
import com.qwerty.pannes.repository.CampusRepository;
import com.qwerty.pannes.repository.InterventionRepository;
import com.qwerty.pannes.repository.PanneRepository;
import com.qwerty.pannes.repository.UniteRepository;
import com.qwerty.pannes.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String MOT_DE_PASSE_DEMO = "demo123";

    private final UtilisateurRepository utilisateurs;
    private final PanneRepository pannes;
    private final InterventionRepository interventions;
    private final CampusRepository campusRepo;
    private final UniteRepository uniteRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(UtilisateurRepository utilisateurs, PanneRepository pannes,
                           InterventionRepository interventions, CampusRepository campusRepo,
                           UniteRepository uniteRepo, PasswordEncoder encoder) {
        this.utilisateurs = utilisateurs;
        this.pannes = pannes;
        this.interventions = interventions;
        this.campusRepo = campusRepo;
        this.uniteRepo = uniteRepo;
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

        Campus noulLocal = campusRepo.findByNom(StructureInitializer.NOUL_LOCAL).orElseThrow();
        Campus polizu = campusRepo.findByNom(StructureInitializer.POLIZU).orElseThrow();
        Campus leu = campusRepo.findByNom(StructureInitializer.LEU).orElseThrow();

        Unite rectorat = unite(StructureInitializer.RECTORAT);
        Unite automatique = unite(StructureInitializer.AUTOMATIQUE);
        Unite sciences = unite(StructureInitializer.SCIENCES);
        Unite fils = unite(StructureInitializer.FILS);
        Unite aerospatial = unite(StructureInitializer.AEROSPATIAL);
        Unite medical = unite(StructureInitializer.MEDICAL);
        Unite etti = unite(StructureInitializer.ETTI);

        Utilisateur maria    = utilisateur("Ionescu", "Maria", "maria.ionescu@exemple.fr", mdp, Role.UTILISATEUR, noulLocal, fils);
        Utilisateur andrei   = utilisateur("Popescu", "Andrei", "andrei.popescu@exemple.fr", mdp, Role.UTILISATEUR, leu, etti);
        Utilisateur ion      = utilisateur("Georgescu", "Ion", "ion.georgescu@exemple.fr", mdp, Role.RESPONSABLE, noulLocal, null);
        Utilisateur elena    = utilisateur("Dumitru", "Elena", "elena.dumitru@exemple.fr", mdp, Role.RESPONSABLE, noulLocal, null);
        Utilisateur radu     = utilisateur("Marin", "Radu", "radu.marin@exemple.fr", mdp, Role.RESPONSABLE, polizu, null);
        Utilisateur cristina = utilisateur("Stan", "Cristina", "cristina.stan@exemple.fr", mdp, Role.RESPONSABLE, leu, null);
        utilisateur("Système", "Admin", "admin@exemple.fr", mdp, Role.ADMIN, null, null);
        // *   admin@exemple.fr             - ADMIN


        LocalDateTime maintenant = LocalDateTime.now();

        nouvelle("Projecteur ne fonctionne pas", "Le projecteur de la salle du Sénat ne s'allume pas et affiche un écran noir.",
                "Salle du Sénat, 1er étage", rectorat, Categorie.AUDIOVISUEL, Priorite.HAUTE, maria, maintenant.minusDays(1));
        nouvelle("Prise électrique hors service", "La prise murale près du bureau du professeur ne fournit plus de courant.",
                "B204", automatique, Categorie.ELECTRICITE, Priorite.MOYENNE, andrei, maintenant.minusDays(2));
        nouvelle("Lumière qui clignote", "Le néon du fond de la salle clignote en permanence.",
                "A1012", fils, Categorie.ELECTRICITE, Priorite.BASSE, maria, maintenant.minusDays(3));
        nouvelle("Imprimante bloquée", "L'imprimante du couloir affiche un bourrage papier permanent.",
                "C2010", etti, Categorie.INFORMATIQUE, Priorite.MOYENNE, andrei, maintenant.minusHours(5));
        nouvelle("Écran du poste enseignant cassé", "L'écran du poste de l'enseignant est fissuré et ne s'allume plus.",
                "Salle P108", aerospatial, Categorie.INFORMATIQUE, Priorite.HAUTE, maria, maintenant.minusDays(2));

        enCours("Climatisation défaillante", "La climatisation souffle de l'air chaud.",
                "C3205", fils, Categorie.CLIMATISATION, Priorite.MOYENNE, maria, ion, maintenant.minusDays(4));
        enCours("Table endommagée", "Un pied de table est cassé, la table est instable.",
                "C3201", sciences, Categorie.MOBILIER, Priorite.BASSE, andrei, elena, maintenant.minusDays(6));
        enCours("Vidéoprojecteur sans image", "Le vidéoprojecteur affiche une image floue qui saute.",
                "Amphithéâtre P2", medical, Categorie.AUDIOVISUEL, Priorite.MOYENNE, maria, radu, maintenant.minusDays(3));

        resolue("Ordinateur ne s'allume pas", "Le poste n°12 ne démarre plus.",
                "C3201", fils, Categorie.INFORMATIQUE, Priorite.HAUTE, maria, ion, maintenant.minusDays(10));
        resolue("Accès Wi-Fi impossible", "Impossible de se connecter au réseau Wi-Fi dans l'amphithéâtre.",
                "A1024", etti, Categorie.RESEAU, Priorite.MOYENNE, andrei, cristina, maintenant.minusDays(12));

        log.info("Données d'exemple chargées : {} utilisateurs, {} pannes, {} interventions.",
                utilisateurs.count(), pannes.count(), interventions.count());
    }

    private Unite unite(String nom) {
        return uniteRepo.findByNom(nom).orElseThrow();
    }

    private Utilisateur utilisateur(String nom, String prenom, String email, String mdp, Role role,
                                    Campus campus, Unite unite) {
        Utilisateur u = new Utilisateur(nom, prenom, email, mdp, role);
        u.setCampus(campus);
        u.setUnite(unite);
        return utilisateurs.save(u);
    }

    private Panne nouvelle(String titre, String description, String lieu, Unite unite, Categorie categorie,
                           Priorite priorite, Utilisateur signaleur, LocalDateTime date) {
        Panne p = new Panne(titre, description, lieu, categorie, priorite, signaleur);
        p.setUnite(unite);
        p.setDateSignalement(date);
        p = pannes.save(p);
        interventions.save(new Intervention(p, signaleur, TypeAction.SIGNALEMENT, null, date));
        return p;
    }

    private Panne enCours(String titre, String description, String lieu, Unite unite, Categorie categorie,
                          Priorite priorite, Utilisateur signaleur, Utilisateur responsable, LocalDateTime date) {
        Panne p = nouvelle(titre, description, lieu, unite, categorie, priorite, signaleur, date);
        p.setResponsable(responsable);
        p.setStatut(Statut.IN_PROGRESS);
        p = pannes.save(p);
        interventions.save(new Intervention(p, responsable, TypeAction.PRISE_EN_CHARGE, null, date.plusHours(2)));
        return p;
    }

    private Panne resolue(String titre, String description, String lieu, Unite unite, Categorie categorie,
                          Priorite priorite, Utilisateur signaleur, Utilisateur responsable, LocalDateTime date) {
        Panne p = enCours(titre, description, lieu, unite, categorie, priorite, signaleur, responsable, date);
        LocalDateTime dateResolution = date.plusDays(1);
        p.setStatut(Statut.DONE);
        p.setDateResolution(dateResolution);
        p = pannes.save(p);
        interventions.save(new Intervention(p, responsable, TypeAction.RESOLUTION,
                "Intervention terminée, problème résolu.", dateResolution));
        return p;
    }
}