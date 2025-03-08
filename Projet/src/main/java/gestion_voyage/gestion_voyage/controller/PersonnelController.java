package gestion_voyage.gestion_voyage.controller;
import gestion_voyage.gestion_voyage.dto.PersonnelDto;
import gestion_voyage.gestion_voyage.service.PersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personnels")


public class PersonnelController {


    @Autowired
    private PersonnelService personnelService;

    @PostMapping
    public ResponseEntity<PersonnelDto> createPersonnel(@RequestBody PersonnelDto personnelDto) {
        PersonnelDto createdPersonnel = personnelService.createPersonnel(personnelDto);
        return ResponseEntity.ok(createdPersonnel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonnelDto> updatePersonnel(@PathVariable Long id, @RequestBody PersonnelDto personnelDto) {
        PersonnelDto updatedPersonnel = personnelService.updatePersonnel(id, personnelDto);
        if (updatedPersonnel != null) {
            return ResponseEntity.ok(updatedPersonnel);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonnelDto> getPersonnelById(@PathVariable Long id) {
        PersonnelDto personnelDto = personnelService.getPersonnelById(id);
        if (personnelDto != null) {
            return ResponseEntity.ok(personnelDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonnel(@PathVariable Long id) {
        personnelService.deletePersonnel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PersonnelDto>> getAllPersonnels() {
        List<PersonnelDto> personnels = personnelService.getAllPersonnels();
        return ResponseEntity.ok(personnels);
    }

    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<PersonnelDto> getPersonnelByMatricule(@PathVariable String matricule) {
        PersonnelDto personnelDto = personnelService.getPersonnelByMatricule(matricule);
        if (personnelDto != null) {
            return ResponseEntity.ok(personnelDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PersonnelDto>> getPersonnelsByType(@PathVariable String type) {
        List<PersonnelDto> personnels = personnelService.getPersonnelsByType(type);
        return ResponseEntity.ok(personnels);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<PersonnelDto> getPersonnelByEmail(@PathVariable String email) {
        PersonnelDto personnelDto = personnelService.getPersonnelByEmail(email);
        if (personnelDto != null) {
            return ResponseEntity.ok(personnelDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/nom/{nom}")
    public ResponseEntity<List<PersonnelDto>> getPersonnelsByNom(@PathVariable String nom) {
        List<PersonnelDto> personnels = personnelService.getPersonnelsByNom(nom);
        return ResponseEntity.ok(personnels);
    }

    @GetMapping("/prenom/{prenom}")
    public ResponseEntity<List<PersonnelDto>> getPersonnelsByPrenom(@PathVariable String prenom) {
        List<PersonnelDto> personnels = personnelService.getPersonnelsByPrenom(prenom);
        return ResponseEntity.ok(personnels);
    }

    @GetMapping("/telephone/{telephone}")
    public ResponseEntity<List<PersonnelDto>> getPersonnelsByTelephone(@PathVariable String telephone) {
        List<PersonnelDto> personnels = personnelService.getPersonnelsByTelephone(telephone);
        return ResponseEntity.ok(personnels);
    }
}


