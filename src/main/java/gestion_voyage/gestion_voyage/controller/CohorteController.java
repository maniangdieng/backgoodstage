package gestion_voyage.gestion_voyage.controller;
import gestion_voyage.gestion_voyage.dto.CohorteDto;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.mapper.CohorteMapper;
import gestion_voyage.gestion_voyage.service.CohorteService;
import io.micrometer.core.instrument.config.validate.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cohortes")

public class CohorteController {


    private final CohorteService cohorteService;
    private final CohorteMapper cohorteMapper;

    @Autowired
    public CohorteController(CohorteService cohorteService, CohorteMapper cohorteMapper) {
        this.cohorteService = cohorteService;
        this.cohorteMapper = cohorteMapper;
    }

    @GetMapping
    public List<CohorteDto> getAllCohortes() {
        return cohorteService.getAllCohortes().stream()
                .map(cohorteMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CohorteDto> getCohorteById(@PathVariable Long id) {
        Optional<Cohorte> cohorte = cohorteService.getCohorteById(id);
        return cohorte.map(value -> ResponseEntity.ok(cohorteMapper.toDto(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
  // Ajoutez cette méthode pour vérifier l'existence d'une cohorte par année
  @GetMapping("/exists")
  public ResponseEntity<Boolean> checkCohorteExistsByAnnee(@RequestParam int annee) {
    boolean exists = cohorteService.existsByAnnee(annee);
    return ResponseEntity.ok(exists);
  }


    @PostMapping
    public ResponseEntity<?> createCohorte(@RequestBody CohorteDto cohorteDto) {
      try {
        // Vérifier si une cohorte existe déjà pour cette année
        if (cohorteService.existsByAnnee(cohorteDto.getAnnee())) {
          return ResponseEntity.badRequest().body("Une cohorte existe déjà pour cette année.");
        }

        Cohorte cohorte = cohorteMapper.toEntity(cohorteDto);
        Cohorte savedCohorte = cohorteService.saveCohorte(cohorte);
        return ResponseEntity.status(HttpStatus.CREATED).body(cohorteMapper.toDto(savedCohorte));
      } catch (ValidationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCohorte(@PathVariable Long id, @RequestBody CohorteDto cohorteDto) {
      try {
        Cohorte cohorte = cohorteMapper.toEntity(cohorteDto);
        cohorte.setId(id);
        Cohorte updatedCohorte = cohorteService.saveCohorte(cohorte);
        return ResponseEntity.ok(cohorteMapper.toDto(updatedCohorte));
      } catch (ValidationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCohorte(@PathVariable Long id) {
        if (cohorteService.getCohorteById(id).isPresent()) {
            cohorteService.deleteCohorteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
