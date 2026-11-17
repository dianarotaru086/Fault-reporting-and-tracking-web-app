package com.qwerty.pannes.web;

import com.qwerty.pannes.service.StructureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/structure")
public class StructureController {

    private final StructureService service;

    public StructureController(StructureService service) {
        this.service = service;
    }

    @GetMapping
    public List<CampusDto> structure() {
        return service.structure();
    }
}