package gestion_voyage.gestion_voyage.service;
import gestion_voyage.gestion_voyage.entity.Cohorte;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CohorteService {

    // Récupérer toutes les cohortes
    List<Cohorte> getAllCohortes();

    // Récupérer une cohorte par son ID
    Optional<Cohorte> getCohorteById(Long id);

    // Récupérer une cohorte par année
    Optional<Cohorte> getCohorteByAnnee(Integer annee);

    // Récupérer des cohortes dont la date de clôture est avant une certaine date
    List<Cohorte> getCohortesByDateClotureDefBefore(LocalDate date);

    // Récupérer des cohortes dont la date de semi-clôture est après une certaine date
    List<Cohorte> getCohortesByDateSemiClotureAfter(LocalDate date);

    // Sauvegarder une nouvelle cohorte
    Cohorte saveCohorte(Cohorte cohorte);

    // Supprimer une cohorte par son ID
    void deleteCohorteById(Long id);

    // Supprimer une cohorte par année
    void deleteCohorteByAnnee(Integer annee);
}