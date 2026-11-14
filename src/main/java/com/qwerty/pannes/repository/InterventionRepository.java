package com.qwerty.pannes.repository;

import com.qwerty.pannes.model.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    List<Intervention> findByPanneIdOrderByDateInterventionAsc(Long panneId);
}
