package gestion_voyage.gestion_voyage.repository;

import gestion_voyage.gestion_voyage.entity.Personnel;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {


    // Trouver un personnel par son matricule (unique)
    @Transactional(readOnly = true)
    Optional<Personnel> findByMatricule(String matricule);

    // Trouver tous les personnels par type (ex : enseignant, administrateur)
    @Transactional(readOnly = true)
    List<Personnel> findByType(String type);

    // Trouver un personnel par son email (unique)
    @Transactional(readOnly = true)
    Optional<Personnel> findByEmail(String email);

    // Trouver tous les personnels ayant un nom spécifique
    @Transactional(readOnly = true)
    List<Personnel> findByNom(String nom);

    // Trouver tous les personnels ayant un prénom spécifique
    @Transactional(readOnly = true)
    List<Personnel> findByPrenom(String prenom);

    // Trouver tous les personnels par téléphone
    @Transactional(readOnly = true)
    List<Personnel> findByTelephone(String telephone);


}
