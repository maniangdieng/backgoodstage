package gestion_voyage.gestion_voyage.controller;
import gestion_voyage.gestion_voyage.dto.UtilisateurDto;
import gestion_voyage.gestion_voyage.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")

public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Autowired
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // Création d'un nouvel utilisateur
    @PostMapping
    public ResponseEntity<UtilisateurDto> createUtilisateur(@RequestBody UtilisateurDto utilisateurDto) {
        try {
            UtilisateurDto createdUtilisateur = utilisateurService.createUtilisateur(utilisateurDto, utilisateurDto.getMotDePasse());
            return new ResponseEntity<>(createdUtilisateur, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Mise à jour d'un utilisateur existant
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDto> updateUtilisateur(@PathVariable Long id, @RequestBody UtilisateurDto utilisateurDto) {
        try {
            UtilisateurDto updatedUtilisateur = utilisateurService.updateUtilisateur(id, utilisateurDto, utilisateurDto.getMotDePasse());
            return new ResponseEntity<>(updatedUtilisateur, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Récupération d'un utilisateur par ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDto> getUtilisateurById(@PathVariable Long id) {
        try {
            UtilisateurDto utilisateurDto = utilisateurService.getUtilisateurById(id);
            return new ResponseEntity<>(utilisateurDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Suppression d'un utilisateur par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        try {
            utilisateurService.deleteUtilisateur(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Récupération de tous les utilisateurs
    @GetMapping
    public ResponseEntity<List<UtilisateurDto>> getAllUtilisateurs() {
        List<UtilisateurDto> utilisateurs = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    // Récupération d'un utilisateur par email
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDto> getUtilisateurByEmail(@PathVariable String email) {
        try {
            UtilisateurDto utilisateurDto = utilisateurService.getUtilisateurByEmail(email);
            return new ResponseEntity<>(utilisateurDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Récupération d'un utilisateur par login
    @GetMapping("/login/{login}")
    public ResponseEntity<UtilisateurDto> getUtilisateurByLogin(@PathVariable String login) {
        try {
            UtilisateurDto utilisateurDto = utilisateurService.getUtilisateurByLogin(login);
            return new ResponseEntity<>(utilisateurDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}