package gestion_voyage.gestion_voyage.service;
import gestion_voyage.gestion_voyage.dto.PersonnelDto;
import java.util.List;


public interface PersonnelService {

    // Créer un nouveau personnel
    PersonnelDto createPersonnel(PersonnelDto personnelDto);

    // Mettre à jour un personnel existant par ID
    PersonnelDto updatePersonnel(Long id, PersonnelDto personnelDto);

    // Récupérer un personnel par son ID
    PersonnelDto getPersonnelById(Long id);

    // Supprimer un personnel par son ID
    void deletePersonnel(Long id);

    // Récupérer tous les personnels
    List<PersonnelDto> getAllPersonnels();

    // Récupérer un personnel par son matricule (unique)
    PersonnelDto getPersonnelByMatricule(String matricule);

    // Récupérer tous les personnels par type (exemple : enseignant, administrateur)
    List<PersonnelDto> getPersonnelsByType(String type);

    // Récupérer un personnel par son email (unique)
    PersonnelDto getPersonnelByEmail(String email);

    // Récupérer tous les personnels par nom
    List<PersonnelDto> getPersonnelsByNom(String nom);

    // Récupérer tous les personnels par prénom
    List<PersonnelDto> getPersonnelsByPrenom(String prenom);

    // Récupérer tous les personnels par numéro de téléphone
    List<PersonnelDto> getPersonnelsByTelephone(String telephone);
}