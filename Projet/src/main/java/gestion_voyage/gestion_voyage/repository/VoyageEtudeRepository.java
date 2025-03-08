package gestion_voyage.gestion_voyage.repository;

import gestion_voyage.gestion_voyage.entity.VoyageEtude;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository


public interface VoyageEtudeRepository extends JpaRepository<VoyageEtude, Long>  {

    // Méthode pour trouver des voyages d'étude par statut
    List<VoyageEtude> findByStatut(String statut);

    // Méthode pour trouver des voyages d'étude par année
    List<VoyageEtude> findByAnnee(Integer annee);

    // Méthode avec pagination pour trouver des voyages d'étude par statut
    Page<VoyageEtude> findByStatut(String statut, Pageable pageable);

    // Méthode avec pagination pour trouver des voyages d'étude par année
    Page<VoyageEtude> findByAnnee(Integer annee, Pageable pageable);

    // Méthode pour vérifier l'existence d'un voyage d'étude avec des critères spécifiques
    boolean existsByDateVoyageAndDateRetourAndSession(LocalDate dateVoyage, LocalDate dateRetour, String session);

    // Méthode pour vérifier l'existence d'un voyage d'étude avec des critères spécifiques, à l'exception d'un ID donné
    boolean existsByDateVoyageAndDateRetourAndSessionAndIdNot(LocalDate dateVoyage, LocalDate dateRetour, String session, Long id);
}