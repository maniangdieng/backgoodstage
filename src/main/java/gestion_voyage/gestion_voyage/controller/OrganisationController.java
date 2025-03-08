package gestion_voyage.gestion_voyage.controller;

import gestion_voyage.gestion_voyage.entity.Organisation;
import gestion_voyage.gestion_voyage.service.OrganisationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

    @Autowired
    private OrganisationService organisationService;

    // Créer une nouvelle organisation
    @PostMapping
    public ResponseEntity<Organisation> createOrganisation(@RequestBody Organisation organisation) {
        Organisation createdOrganisation = organisationService.saveOrganisation(organisation);
        return new ResponseEntity<>(createdOrganisation, HttpStatus.CREATED);
    }

    // Obtenir une organisation par ID
    @GetMapping("/{idOrganisation}")
    public ResponseEntity<Organisation> getOrganisationById(@PathVariable Long idOrganisation) {
        Optional<Organisation> organisation = organisationService.getOrganisationById(idOrganisation);
        if (organisation.isPresent()) {
            return new ResponseEntity<>(organisation.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtenir toutes les organisations
    @GetMapping
    public ResponseEntity<List<Organisation>> getAllOrganisations() {
        List<Organisation> organisations = organisationService.getAllOrganisations();
        return new ResponseEntity<>(organisations, HttpStatus.OK);
    }

    // Rechercher des organisations par nom (recherche partielle)
    @GetMapping("/search")
    public ResponseEntity<List<Organisation>> searchOrganisations(@RequestParam String keyword) {
        List<Organisation> organisations = organisationService.searchOrganisationsByNom(keyword);
        return new ResponseEntity<>(organisations, HttpStatus.OK);
    }

    // Rechercher une organisation par email
    @GetMapping("/email/{email}")
    public ResponseEntity<Organisation> getOrganisationByEmail(@PathVariable String email) {
        Optional<Organisation> organisation = organisationService.getOrganisationByEmail(email);
        if (organisation.isPresent()) {
            return new ResponseEntity<>(organisation.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Rechercher une organisation par téléphone
    @GetMapping("/telephone/{telephone}")
    public ResponseEntity<Organisation> getOrganisationByTelephone(@PathVariable String telephone) {
        Optional<Organisation> organisation = organisationService.getOrganisationByTelephone(telephone);
        if (organisation.isPresent()) {
            return new ResponseEntity<>(organisation.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Mettre à jour une organisation
    @PutMapping("/{idOrganisation}")
    public ResponseEntity<Organisation> updateOrganisation(@PathVariable Long idOrganisation, @RequestBody Organisation organisation) {
        Optional<Organisation> existingOrganisation = organisationService.getOrganisationById(idOrganisation);
        if (!existingOrganisation.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Organisation updatedOrganisation = existingOrganisation.get();
        updatedOrganisation.setNomOrganisation(organisation.getNomOrganisation());
        updatedOrganisation.setEmail(organisation.getEmail());
        updatedOrganisation.setTelephone(organisation.getTelephone());

        Organisation savedOrganisation = organisationService.saveOrganisation(updatedOrganisation);
        return new ResponseEntity<>(savedOrganisation, HttpStatus.OK);
    }

    // Supprimer une organisation par ID
    @DeleteMapping("/{idOrganisation}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable Long idOrganisation) {
        Optional<Organisation> existingOrganisation = organisationService.getOrganisationById(idOrganisation);
        if (!existingOrganisation.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        organisationService.deleteOrganisationById(idOrganisation);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Supprimer une organisation par nom
    @DeleteMapping("/nom/{nomOrganisation}")
    public ResponseEntity<Void> deleteOrganisationByNom(@PathVariable String nomOrganisation) {
        organisationService.deleteOrganisationByNom(nomOrganisation);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}