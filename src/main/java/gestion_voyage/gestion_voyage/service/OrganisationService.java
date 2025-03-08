package gestion_voyage.gestion_voyage.service;

import gestion_voyage.gestion_voyage.entity.Organisation;
import java.util.List;
import java.util.Optional;
public interface OrganisationService {


    // Récupérer toutes les organisations
    List<Organisation> getAllOrganisations();

    // Récupérer une organisation par son ID
    Optional<Organisation> getOrganisationById(Long id);

    // Récupérer une organisation par son nom
    Optional<Organisation> getOrganisationByNom(String nomOrganisation);

    // Rechercher des organisations dont le nom contient un certain mot-clé
    List<Organisation> searchOrganisationsByNom(String keyword);

    // Récupérer une organisation par son email
    Optional<Organisation> getOrganisationByEmail(String email);

    // Récupérer une organisation par son numéro de téléphone
    Optional<Organisation> getOrganisationByTelephone(String telephone);

    // Enregistrer une nouvelle organisation ou mettre à jour une organisation existante
    Organisation saveOrganisation(Organisation organisation);

    // Supprimer une organisation par son ID
    void deleteOrganisationById(Long id);

    // Supprimer une organisation par son nom
    void deleteOrganisationByNom(String nomOrganisation);
}