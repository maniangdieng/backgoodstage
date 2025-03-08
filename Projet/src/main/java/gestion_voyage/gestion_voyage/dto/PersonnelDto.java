package gestion_voyage.gestion_voyage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class PersonnelDto  extends UtilisateurDto  {

    Long id;
    @NotNull(message = "Le matricule ne doit pas être nul")
    @Size(min = 3, max = 20, message = "Le matricule doit comporter entre 3 et 20 caractères")
    String matricule;

    @NotNull(message = "Le type de personnel ne doit pas être nul")
    @Size(min = 2, max = 20, message = "Le type de personnel doit comporter entre 2 et 20 caractères")
    String type;

    @NotNull(message = "Les candidatures ne doivent pas être nulles")
    @Size(min = 1, message = "Il doit y avoir au moins une candidature associée")
    Set<Long> candidatureIds;

    @NotNull(message = "Les responsabilités ne doivent pas être nulles")
    Set<Long> responsabiliteIds;

    Long subventionId;

    @NotNull(message = "Le nom ne doit pas être nul")
    @Size(min = 2, max = 50, message = "Le nom doit comporter entre 2 et 50 caractères")
    String nom;

    @NotNull(message = "Le prénom ne doit pas être nul")
    @Size(min = 2, max = 50, message = "Le prénom doit comporter entre 2 et 50 caractères")
    String prenom;

    @NotNull(message = "L'email ne doit pas être nul")
    @Email(message = "Le format de l'email est invalide")
    String email;

    @NotNull(message = "Le login ne doit pas être nul")
    @Size(min = 3, max = 30, message = "Le login doit comporter entre 3 et 30 caractères")
    private String login;

    @NotNull(message = "Le téléphone ne doit pas être nul")
    @Size(min = 8, max = 15, message = "Le numéro de téléphone doit comporter entre 8 et 15 caractères")
    String telephone;

    @NotNull(message = "Le mot de passe ne doit pas être nul")
    @Size(min = 6, max = 20, message = "Le mot de passe doit comporter entre 6 et 100 caractères")
    String motDePasse;


}