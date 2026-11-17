package com.qwerty.pannes.service;

import com.qwerty.pannes.model.Unite;
import com.qwerty.pannes.repository.CampusRepository;
import com.qwerty.pannes.repository.UniteRepository;
import com.qwerty.pannes.web.CampusDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Les campus avec leurs facultés : sert aux listes déroulantes. */
@Service
public class StructureService {

    private final CampusRepository campus;
    private final UniteRepository unites;

    public StructureService(CampusRepository campus, UniteRepository unites) {
        this.campus = campus;
        this.unites = unites;
    }

    @Transactional(readOnly = true)
    public List<CampusDto> structure() {
        List<Unite> toutes = unites.toutes();
        return campus.findAllByOrderByIdAsc().stream()
                .map(c -> new CampusDto(c.getId(), c.getNom(), c.getAdresse(),
                        toutes.stream()
                                .filter(u -> u.getCampus().getId().equals(c.getId()))
                                .map(u -> new CampusDto.UniteDto(u.getId(), u.getNom()))
                                .toList()))
                .toList();
    }
}