package com.qwerty.pannes.repository;

import com.qwerty.pannes.model.Panne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PanneRepository extends JpaRepository<Panne, Long>, JpaSpecificationExecutor<Panne> {

    List<Panne> findBySignaleurIdOrderByDateSignalementDesc(Long signaleurId);

    List<Panne> findByResponsableIdOrderByDateSignalementDesc(Long responsableId);
}
