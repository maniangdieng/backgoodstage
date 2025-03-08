package gestion_voyage.gestion_voyage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // Génère un constructeur sans arguments


public class UtilisateurDto {
    Long id;

    @NotBlank(message = "Le matricule est obligatoire")
    String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    String email;

    @NotBlank(message = "Le login est obligatoire")
    String login;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, incluant une majuscule, une minuscule, un chiffre et un caractère spécial")
    String motDePasse;

    @NotBlank(message = "Le rôle est obligatoire")
    String role;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide")
    String telephone;

}