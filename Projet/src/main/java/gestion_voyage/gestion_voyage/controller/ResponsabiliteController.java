
package gestion_voyage.gestion_voyage.controller;
import gestion_voyage.gestion_voyage.dto.ResponsabiliteDto;
import gestion_voyage.gestion_voyage.entity.Responsabilite;
import gestion_voyage.gestion_voyage.service.ResponsabiliteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/responsabilites")

public class ResponsabiliteController {

    @Autowired
    private ResponsabiliteService responsabiliteService;

    @GetMapping
    public ResponseEntity<List<Responsabilite>> getAllResponsabilites() {
        List<Responsabilite> responsabilites = responsabiliteService.getAllResponsabilites();
        return ResponseEntity.ok(responsabilites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Responsabilite> getResponsabiliteById(@PathVariable Long id) {
        Optional<Responsabilite> responsabilite = responsabiliteService.getResponsabiliteById(id);
        return responsabilite.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/libelle/{libelle}")
    public ResponseEntity<Responsabilite> getResponsabiliteByLibelle(@PathVariable String libelle) {
        Optional<Responsabilite> responsabilite = responsabiliteService.getResponsabiliteByLibelle(libelle);
        return responsabilite.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/actives")
    public ResponseEntity<List<Responsabilite>> getResponsabilitesActives() {
        List<Responsabilite> responsabilites = responsabiliteService.getResponsabilitesActives();
        return ResponseEntity.ok(responsabilites);
    }

    @GetMapping("/dates")
    public ResponseEntity<List<Responsabilite>> getResponsabilitesByDateDebutBetween(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Responsabilite> responsabilites = responsabiliteService.getResponsabilitesByDateDebutBetween(startDate, endDate);
        return ResponseEntity.ok(responsabilites);
    }

    @PostMapping
    public ResponseEntity<Responsabilite> createResponsabilite(@RequestBody Responsabilite responsabilite) {
        Responsabilite savedResponsabilite = responsabiliteService.saveResponsabilite(responsabilite);
        return ResponseEntity.ok(savedResponsabilite);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Responsabilite> updateResponsabilite(@PathVariable Long id, @RequestBody Responsabilite responsabilite) {
        responsabilite.setId(id);
        Responsabilite updatedResponsabilite = responsabiliteService.saveResponsabilite(responsabilite);
        return ResponseEntity.ok(updatedResponsabilite);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponsabiliteById(@PathVariable Long id) {
        responsabiliteService.deleteResponsabiliteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/libelle/{libelle}")
    public ResponseEntity<Void> deleteResponsabiliteByLibelle(@PathVariable String libelle) {
        responsabiliteService.deleteResponsabiliteByLibelle(libelle);
        return ResponseEntity.noContent().build();
    }
}