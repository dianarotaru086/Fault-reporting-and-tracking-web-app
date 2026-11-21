package com.qwerty.pannes.config;

import com.qwerty.pannes.model.Campus;
import com.qwerty.pannes.model.Unite;
import com.qwerty.pannes.repository.CampusRepository;
import com.qwerty.pannes.repository.UniteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Order(1)
public class StructureInitializer implements CommandLineRunner {

    public static final String NOUL_LOCAL = "Nouveau Local (campus principal)";
    public static final String POLIZU = "Polizu";
    public static final String LEU = "Leu";

    public static final String RECTORAT = "Rectorat et services centraux";
    public static final String AUTOMATIQUE = "Automatique et Informatique";
    public static final String SCIENCES = "Sciences appliquées";
    public static final String FILS = "Ingénierie en Langues étrangères";
    public static final String AEROSPATIAL = "Génie aérospatial";
    public static final String MEDICAL = "Génie médical";
    public static final String ETTI = "Électronique, Télécommunications et Technologies de l'Information";

    private final CampusRepository campus;
    private final UniteRepository unites;

    public StructureInitializer(CampusRepository campus, UniteRepository unites) {
        this.campus = campus;
        this.unites = unites;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (campus.count() > 0) {
            return;
        }

        Campus noulLocal = campus.save(new Campus(NOUL_LOCAL, "Splaiul Independenței 313, secteur 6"));
        Campus polizu = campus.save(new Campus(POLIZU, "Rue Gheorghe Polizu 1-7, secteur 1"));
        Campus leu = campus.save(new Campus(LEU, "Boulevard Iuliu Maniu 1-3, secteur 6"));

        unites.save(new Unite(RECTORAT, noulLocal));
        unites.save(new Unite("Génie électrique", noulLocal));
        unites.save(new Unite("Énergétique", noulLocal));
        unites.save(new Unite(AUTOMATIQUE, noulLocal));
        unites.save(new Unite("Génie mécanique et Mécatronique", noulLocal));
        unites.save(new Unite("Génie industriel et Robotique", noulLocal));
        unites.save(new Unite("Ingénierie des Systèmes biotechniques", noulLocal));
        unites.save(new Unite("Transports", noulLocal));
        unites.save(new Unite(SCIENCES, noulLocal));
        unites.save(new Unite("Science et Ingénierie des Matériaux", noulLocal));
        unites.save(new Unite(FILS, noulLocal));
        unites.save(new Unite("Entrepreneuriat, Ingénierie et Management des Affaires", noulLocal));


        unites.save(new Unite("Génie chimique et Biotechnologies", polizu));
        unites.save(new Unite(AEROSPATIAL, polizu));
        unites.save(new Unite(MEDICAL, polizu));

        unites.save(new Unite(ETTI, leu));
    }
}