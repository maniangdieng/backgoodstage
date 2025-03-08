package gestion_voyage.gestion_voyage.controller;

import gestion_voyage.gestion_voyage.dto.SubventionDto;
import gestion_voyage.gestion_voyage.service.SubventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/subvention")


public class SubventionController {


    @Autowired
    private SubventionService service;

    @PostMapping
    public ResponseEntity<SubventionDto> createSubvention(@RequestBody SubventionDto subventionDto) {
        SubventionDto savedSubvention = service.create(subventionDto);
        return new ResponseEntity<>(savedSubvention, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubventionDto> getSubventionById(@PathVariable Long id) {
        SubventionDto subventionDto = service.getById(id);
        return new ResponseEntity<>(subventionDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<SubventionDto>> getAllSubventions() {
        List<SubventionDto> subventions = service.getAll();
        return new ResponseEntity<>(subventions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubventionDto> updateSubvention(@PathVariable Long id, @RequestBody SubventionDto subventionDto) {
        SubventionDto updatedSubvention = service.update(id, subventionDto);
        return new ResponseEntity<>(updatedSubvention, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubventionDto> partialUpdateSubvention(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        SubventionDto updatedSubvention = service.partialUpdate(id, updates);
        return new ResponseEntity<>(updatedSubvention, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubvention(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}