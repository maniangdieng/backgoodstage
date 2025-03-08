package gestion_voyage.gestion_voyage.repository;
import gestion_voyage.gestion_voyage.entity.Responsabilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository

public interface ResponsabiliteRepository extends JpaRepository<Responsabilite, Long> {


    // Recherche une responsabilité par son libellé
    Optional<Responsabilite> findByLibelle(String libelle);

    // Trouve toutes les responsabilités dont la date de fin est null (encore actives)
    List<Responsabilite> findByDateFinIsNull();

    // Trouve toutes les responsabilités dont la date de début est entre deux dates données
    List<Responsabilite> findByDateDebutBetween(LocalDate startDate, LocalDate endDate);

    // Supprime une responsabilité par son libellé
    void deleteByLibelle(String libelle);
}