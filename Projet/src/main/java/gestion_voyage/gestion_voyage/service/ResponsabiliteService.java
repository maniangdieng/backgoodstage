package gestion_voyage.gestion_voyage.service;


import gestion_voyage.gestion_voyage.entity.Responsabilite;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface ResponsabiliteService {

    // Méthode pour obtenir toutes les responsabilités
    List<Responsabilite> getAllResponsabilites();

    // Méthode pour obtenir une responsabilité par son ID
    Optional<Responsabilite> getResponsabiliteById(Long id);

    // Méthode pour obtenir une responsabilité par son libellé
    Optional<Responsabilite> getResponsabiliteByLibelle(String libelle);

    // Méthode pour rechercher les responsabilités actives (celles sans date de fin)
    List<Responsabilite> getResponsabilitesActives();

    // Méthode pour rechercher les responsabilités dont la date de début est entre deux dates données
    List<Responsabilite> getResponsabilitesByDateDebutBetween(LocalDate startDate, LocalDate endDate);

    // Méthode pour enregistrer ou mettre à jour une responsabilité
    Responsabilite saveResponsabilite(Responsabilite responsabilite);

    // Méthode pour supprimer une responsabilité par son ID
    void deleteResponsabiliteById(Long id);

    // Méthode pour supprimer une responsabilité par son libellé
    void deleteResponsabiliteByLibelle(String libelle);
}