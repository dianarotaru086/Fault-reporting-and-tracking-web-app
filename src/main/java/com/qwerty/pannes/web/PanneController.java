package com.qwerty.pannes.web;

import com.qwerty.pannes.model.Categorie;
import com.qwerty.pannes.model.Priorite;
import com.qwerty.pannes.model.Statut;
import com.qwerty.pannes.service.PanneService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PanneController {

    public record Option(String code, String libelle) {
    }

    private final PanneService service;

    public PanneController(PanneService service) {
        this.service = service;
    }

    /** Signaler une nouvelle panne. @Valid déclenche la validation de PanneRequest. */
    @PostMapping("/pannes")
    @ResponseStatus(HttpStatus.CREATED)
    public PanneDto signaler(@Valid @RequestBody PanneRequest demande, HttpSession session) {
        return service.signaler(demande, Connexion.exiger(session));
    }

    /**
     * La liste des pannes avec recherche et filtres, tous facultatifs :
     * /api/pannes?q=projecteur&categorie=ELECTRICITE&statut=NEW&priorite=HAUTE
     */
    @GetMapping("/pannes")
    public List<PanneDto> lister(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) Categorie categorie,
                                 @RequestParam(required = false) Statut statut,
                                 @RequestParam(required = false) Priorite priorite,
                                 HttpSession session) {
        Connexion.exiger(session);
        return service.lister(q, categorie, statut, priorite);
    }

    /** Les listes de choix (catégories, priorités) pour remplir les menus déroulants. */
    @GetMapping("/references")
    public Map<String, List<Option>> references(HttpSession session) {
        Connexion.exiger(session);
        return Map.of(
                "categories", Arrays.stream(Categorie.values())
                        .map(c -> new Option(c.name(), c.getLibelle())).toList(),
                "priorites", Arrays.stream(Priorite.values())
                        .map(p -> new Option(p.name(), p.getLibelle())).toList(),
                "statuts", Arrays.stream(Statut.values())
                        .map(s -> new Option(s.name(), s.getLibelle())).toList());
    }
    /** Le détail d'une panne, avec l'historique et les droits de l'utilisateur. */
    @GetMapping("/pannes/{id}")
    public PanneDetailDto detail(@PathVariable Long id, HttpSession session) {
        return service.detail(id, Connexion.exiger(session));
    }

    /** Prendre une panne en charge (règle : refusé si elle n'est plus New). */
    @PostMapping("/pannes/{id}/prise-en-charge")
    public PanneDetailDto prendreEnCharge(@PathVariable Long id, HttpSession session) {
        return service.prendreEnCharge(id, Connexion.exiger(session));
    }

    /** Marquer une panne comme résolue (avec un commentaire facultatif). */
    @PostMapping("/pannes/{id}/resolution")
    public PanneDetailDto resoudre(@PathVariable Long id,
                                   @Valid @RequestBody ResolutionRequest demande,
                                   HttpSession session) {
        return service.resoudre(id, Connexion.exiger(session), demande);
    }
    /** Mes pannes : celles que j'ai signalées (et, si je suis responsable, celles que je traite). */
    @GetMapping("/pannes/mes-pannes")
    public MesPannesDto mesPannes(HttpSession session) {
        return service.mesPannes(Connexion.exiger(session));
    }

    /** Modifier une panne (même validation que le signalement). */
    @PutMapping("/pannes/{id}")
    public PanneDto modifier(@PathVariable Long id,
                             @Valid @RequestBody PanneRequest demande,
                             HttpSession session) {
        return service.modifier(id, demande, Connexion.exiger(session));
    }

    /** Supprimer une panne. */
    @DeleteMapping("/pannes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, HttpSession session) {
        service.supprimer(id, Connexion.exiger(session));
    }
}
