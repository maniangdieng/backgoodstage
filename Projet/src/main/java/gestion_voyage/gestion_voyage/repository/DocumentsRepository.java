package gestion_voyage.gestion_voyage.repository;
import gestion_voyage.gestion_voyage.entity.Documents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface DocumentsRepository extends JpaRepository<Documents,  Long> {


    // Méthode personnalisée pour trouver les documents par nom de fichier
    List<Documents> findByNomFichier(String nomFichier);

    // Méthode personnalisée pour trouver les documents par date de début
    List<Documents> findByDateDebut(LocalDate dateDebut);

    // Méthode personnalisée pour trouver les documents par date de fin
    List<Documents> findByDateFin(LocalDate dateFin);

    // Méthode personnalisée pour trouver les documents par statut
    List<Documents> findByStatut(String statut);

    // Méthode personnalisée pour trouver les documents par VoyageEtude
    List<Documents> findByVoyageEtudeId(Long voyageEtudeId);

    // Méthode personnalisée pour trouver les documents par Subvention
    List<Documents> findBySubventionId(Long subventionId);

    // Méthode personnalisée pour trouver les documents par Candidature
    List<Documents> findByCandidatureId(Long candidatureId);
}
