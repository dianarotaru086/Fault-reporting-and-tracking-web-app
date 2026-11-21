package com.qwerty.pannes.web;

import java.util.List;

public record MesPannesDto(List<PanneDto> signalees, List<PanneDto> priseEnCharge) {
}