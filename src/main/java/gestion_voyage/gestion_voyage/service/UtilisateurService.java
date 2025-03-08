package gestion_voyage.gestion_voyage.service;
import gestion_voyage.gestion_voyage.dto.UtilisateurDto;
import java.util.List;

public interface UtilisateurService {

    // Crée un nouvel utilisateur
    UtilisateurDto createUtilisateur(UtilisateurDto utilisateurDto, String motDePasse);

    // Met à jour un utilisateur existant
    UtilisateurDto updateUtilisateur(Long id, UtilisateurDto utilisateurDto, String motDePasse);

    // Récupère un utilisateur par son ID
    UtilisateurDto getUtilisateurById(Long id);

    // Supprime un utilisateur par son ID
    void deleteUtilisateur(Long id);

    // Récupère la liste de tous les utilisateurs
    List<UtilisateurDto> getAllUtilisateurs();

    // Récupère un utilisateur par son email
    UtilisateurDto getUtilisateurByEmail(String email);

    // Récupère un utilisateur par son login
    UtilisateurDto getUtilisateurByLogin(String login);
}