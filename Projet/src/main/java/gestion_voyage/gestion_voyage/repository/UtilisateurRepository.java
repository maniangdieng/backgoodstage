package gestion_voyage.gestion_voyage.repository;
import gestion_voyage.gestion_voyage.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {


    // Recherche un utilisateur par son email
    Optional<Utilisateur> findByEmail(String email);

    // Recherche un utilisateur par son login
    Optional<Utilisateur> findByLogin(String login);

    // Vérifie si un utilisateur existe déjà par email
    boolean existsByEmail(String email);

    // Vérifie si un utilisateur existe déjà par login
    boolean existsByLogin(String login);

    // Récupère tous les utilisateurs avec un rôle spécifique
    List<Utilisateur> findByRole(String role);
}