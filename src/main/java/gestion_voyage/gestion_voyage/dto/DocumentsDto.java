package gestion_voyage.gestion_voyage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentsDto {

    Long id; // Identifiant du document

    String statut; // Statut du document

    LocalDate dateDebut;  // Date de début

    LocalDate dateFin;  // Date de fin

    String nomFichier; // Nom du fichier associé au document

    // Ajout des identifiants pour les associations
    Long voyageEtudeId;
    Long subventionId;
    Long candidatureId;


}


