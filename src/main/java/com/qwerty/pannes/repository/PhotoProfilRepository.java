package com.qwerty.pannes.repository;

import com.qwerty.pannes.model.PhotoProfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PhotoProfilRepository extends JpaRepository<PhotoProfil, Long> {

    /** Seulement la date : permet de savoir si une photo existe sans charger l'image. */
    @Query("select p.dateMiseAJour from PhotoProfil p where p.utilisateurId = :id")
    Optional<LocalDateTime> dateMiseAJour(@Param("id") Long id);
}