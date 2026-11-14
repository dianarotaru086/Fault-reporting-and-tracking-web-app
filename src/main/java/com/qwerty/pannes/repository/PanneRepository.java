package com.qwerty.pannes.repository;

import com.qwerty.pannes.model.Panne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * JpaSpecificationExecutor servira pour la recherche et les filtres combinés
 * (titre, catégorie, statut, priorité, lieu).
 */
public interface PanneRepository extends JpaRepository<Panne, Long>, JpaSpecificationExecutor<Panne> {

    List<Panne> findBySignaleurIdOrderByDateSignalementDesc(Long signaleurId);

    List<Panne> findByResponsableIdOrderByDateSignalementDesc(Long responsableId);
}
