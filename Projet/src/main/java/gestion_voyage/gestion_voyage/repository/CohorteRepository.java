package gestion_voyage.gestion_voyage.repository;

import gestion_voyage.gestion_voyage.entity.Cohorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository

public interface CohorteRepository  extends JpaRepository<Cohorte, Long> {

    // Rechercher une cohorte par la date d'ouverture
    Optional<Cohorte> findByDateOuverture(LocalDate dateOuverture);

    // Rechercher une cohorte par année
    Optional<Cohorte> findByAnnee(Integer annee);

    // Rechercher les cohortes dont la date de clôture est avant une date donnée
    List<Cohorte> findByDateClotureDefBefore(LocalDate date);

    // Rechercher les cohortes dont la date de semi-clôture est après une date donnée
    List<Cohorte> findByDateSemiClotureAfter(LocalDate date);

    // Supprimer une cohorte par année
    void deleteByAnnee(Integer annee);
}