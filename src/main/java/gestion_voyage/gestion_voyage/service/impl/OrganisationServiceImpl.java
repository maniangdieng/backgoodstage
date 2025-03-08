
package gestion_voyage.gestion_voyage.service.impl;


import gestion_voyage.gestion_voyage.entity.Organisation;
import gestion_voyage.gestion_voyage.repository.OrganisationRepository;
import gestion_voyage.gestion_voyage.service.OrganisationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class OrganisationServiceImpl implements OrganisationService {

    @Autowired

    private final OrganisationRepository organisationRepository;

    @Autowired
    public OrganisationServiceImpl(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    // Récupérer toutes les organisations
    @Override
    public List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }

    // Récupérer une organisation par son ID
    @Override
    public Optional<Organisation> getOrganisationById(Long id) {
        return organisationRepository.findById(id);
    }

    // Récupérer une organisation par son nom
    @Override
    public Optional<Organisation> getOrganisationByNom(String nomOrganisation) {
        return organisationRepository.findByNomOrganisation(nomOrganisation);
    }

    // Rechercher des organisations dont le nom contient un mot-clé (recherche partielle)
    @Override
    public List<Organisation> searchOrganisationsByNom(String keyword) {
        return organisationRepository.findByNomOrganisationContaining(keyword);
    }

    // Récupérer une organisation par email
    @Override
    public Optional<Organisation> getOrganisationByEmail(String email) {
        return organisationRepository.findByEmail(email);
    }

    // Récupérer une organisation par numéro de téléphone
    @Override
    public Optional<Organisation> getOrganisationByTelephone(String telephone) {
        return organisationRepository.findByTelephone(telephone);
    }

    // Enregistrer une nouvelle organisation ou mettre à jour une organisation existante
    @Override
    public Organisation saveOrganisation(Organisation organisation) {
        return organisationRepository.save(organisation);
    }

    // Supprimer une organisation par son ID
    @Override
    public void deleteOrganisationById(Long id) {
        organisationRepository.deleteById(id);
    }

    // Supprimer une organisation par son nom
    @Override
    public void deleteOrganisationByNom(String nomOrganisation) {
        organisationRepository.deleteByNomOrganisation(nomOrganisation);
    }
}