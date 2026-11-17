package com.qwerty.pannes.web;

import java.util.List;

public record CampusDto(Long id, String nom, String adresse, List<UniteDto> unites) {

    public record UniteDto(Long id, String nom) {
    }
}