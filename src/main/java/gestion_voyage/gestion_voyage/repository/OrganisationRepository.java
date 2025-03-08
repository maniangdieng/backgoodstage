package gestion_voyage.gestion_voyage.repository;
import gestion_voyage.gestion_voyage.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository


public interface OrganisationRepository extends JpaRepository<Organisation, Long>  {
 // Rechercher une organisation par nom
 Optional<Organisation> findByNomOrganisation(String nomOrganisation);


 // Rechercher des organisations dont le nom contient un certain mot (recherche partielle)
 List<Organisation> findByNomOrganisationContaining(String keyword);

 // Rechercher des organisations par email
 Optional<Organisation> findByEmail(String email);

 // Rechercher des organisations par téléphone
 Optional<Organisation> findByTelephone(String telephone);


 // Supprimer une organisation par nom
 void deleteByNomOrganisation(String nomOrganisation);

}

