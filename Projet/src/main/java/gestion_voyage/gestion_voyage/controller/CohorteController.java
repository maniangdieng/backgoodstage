package gestion_voyage.gestion_voyage.controller;
import gestion_voyage.gestion_voyage.dto.CohorteDto;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.mapper.CohorteMapper;
import gestion_voyage.gestion_voyage.service.CohorteService;
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

    @PostMapping

    public ResponseEntity<CohorteDto> createCohorte(@RequestBody CohorteDto cohorteDto) {
        if (cohorteDto.getAnnee() == null) {
            return ResponseEntity.badRequest().body(null); // ou personnalisez le message d'erreur
        }

        Cohorte cohorte = cohorteMapper.toEntity(cohorteDto);
        Cohorte savedCohorte = cohorteService.saveCohorte(cohorte);
        return ResponseEntity.status(HttpStatus.CREATED).body(cohorteMapper.toDto(savedCohorte));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CohorteDto> updateCohorte(@PathVariable Long id, @RequestBody CohorteDto cohorteDto) {
        Optional<Cohorte> existingCohorte = cohorteService.getCohorteById(id);
        if (existingCohorte.isPresent()) {
            Cohorte cohorte = cohorteMapper.toEntity(cohorteDto);
            cohorte.setId(id); // Assurez-vous que l'ID est mis à jour
            return ResponseEntity.ok(cohorteMapper.toDto(cohorteService.saveCohorte(cohorte)));
        } else {
            return ResponseEntity.notFound().build();
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