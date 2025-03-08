package gestion_voyage.gestion_voyage.dto;

import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationDto {

    // Identifiant de l'organisation

    private Long idOrganisation;
    // Nom de l'organisation
    String Organisation;
    // Email de l'organisation
    String email;
    // Téléphone de l'organisation
    String telephone;

    // Ensemble des DTOs de responsabilités, servant de lien indirect avec le Personnel

    Set<ResponsabiliteDto> responsabilites = new HashSet<>();
}