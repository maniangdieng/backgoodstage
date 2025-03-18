package gestion_voyage.gestion_voyage.repository;

import gestion_voyage.gestion_voyage.entity.Candidature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {


    List<Candidature> findByStatut(String statut);

    List<Candidature> findByDateDepot(LocalDate dateDepot);

    List<Candidature> findByDateDebut(LocalDate dateDebut);



    List<Candidature> findByDateFin(LocalDate dateFin);

    List<Candidature> findByDestination(String destination);

    List<Candidature> findByCommentaire(String commentaire);

    @Query("SELECT c FROM Candidature c WHERE c.statut = :statut AND c.dateDepot = :dateDepot")
    List<Candidature> findByStatutAndDateDepot(@Param("statut") String statut,
                                               @Param("dateDepot") LocalDate dateDepot);

    // Méthode personnalisée pour obtenir des candidatures avec pagination
    Page<Candidature> findByStatut(String statut, Pageable pageable);

    // Recherche avancée par critères
    @Query("SELECT c FROM Candidature c WHERE (:statut IS NULL OR c.statut = :statut) " +
            "AND (:dateDebut IS NULL OR c.dateDebut = :dateDebut) " +
            "AND (:dateFin IS NULL OR c.dateFin = :dateFin)")
    List<Candidature> findByCriteria(@Param("statut") String statut,
                                     @Param("dateDebut") LocalDate dateDebut,
                                     @Param("dateFin") LocalDate dateFin);


  @Query("SELECT c FROM Candidature c WHERE c.personnel.email = :email")
  List<Candidature> findByPersonnelEmail(@Param("email") String email);


  @Query("SELECT c FROM Candidature c WHERE c.personnel.id = :personnelId")
  List<Candidature> findByPersonnelId(@Param("personnelId") Long personnelId);
}
