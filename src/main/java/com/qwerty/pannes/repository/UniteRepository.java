package com.qwerty.pannes.repository;

import com.qwerty.pannes.model.Unite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UniteRepository extends JpaRepository<Unite, Long> {

    /** Toutes les unités avec leur campus, en une seule requête. */
    @Query("select u from Unite u join fetch u.campus order by u.nom")
    List<Unite> toutes();

    Optional<Unite> findByNom(String nom);
}