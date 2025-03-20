package gestion_voyage.gestion_voyage.controller;

import gestion_voyage.gestion_voyage.dto.VoyageEtudeDto;
import gestion_voyage.gestion_voyage.service.VoyageEtudeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/voyages-etudes")

public class VoyageEtudeController {



    private final VoyageEtudeService service;

    // POST: Ajouter un voyage d'étude
    @PostMapping
    public ResponseEntity<VoyageEtudeDto> createVoyageEtude(@RequestBody VoyageEtudeDto voyageEtudeDto) {
        VoyageEtudeDto savedVoyageEtude = service.create(voyageEtudeDto);
        return new ResponseEntity<>(savedVoyageEtude, HttpStatus.CREATED);
    }

    // GET: Récupérer tous les voyages d'étude
    @GetMapping
    public ResponseEntity<List<VoyageEtudeDto>> getAllVoyagesEtudes() {
        List<VoyageEtudeDto> voyages = service.getAllVoyagesEtudes();
        return new ResponseEntity<>(voyages, HttpStatus.OK);
    }

    // GET: Récupérer un voyage d'étude par ID
    @GetMapping("/{id}")
    public ResponseEntity<VoyageEtudeDto> getVoyageEtudeById(@PathVariable Long id) {
        return service.getVoyageEtudeById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // PUT: Mettre à jour un voyage d'étude
    @PutMapping("/{id}")
    public ResponseEntity<VoyageEtudeDto> updateVoyageEtude(@PathVariable Long id, @RequestBody VoyageEtudeDto voyageEtudeDto) {
        return service.updateVoyageEtude(id, voyageEtudeDto)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // DELETE: Supprimer un voyage d'étude par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoyageEtude(@PathVariable Long id) {
        if (service.deleteVoyageEtude(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}