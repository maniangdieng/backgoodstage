package gestion_voyage.gestion_voyage.repository;


import gestion_voyage.gestion_voyage.entity.Subvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubventionRepository extends JpaRepository<Subvention, Long> {
    // Méthode personnalisée pour trouver les subventions par statut
    List<Subvention> findByStatut(String statut);

    // Méthode personnalisée pour trouver les subventions par type d'activité
    List<Subvention> findByTypeActivite(String typeActivite);
}