package com.qwerty.pannes.repository;

import com.qwerty.pannes.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampusRepository extends JpaRepository<Campus, Long> {

    List<Campus> findAllByOrderByIdAsc();

    Optional<Campus> findByNom(String nom);
}