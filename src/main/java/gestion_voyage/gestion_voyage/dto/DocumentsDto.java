package gestion_voyage.gestion_voyage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentsDto {

    private Long id; // Identifiant du document
    private String statut; // Statut du document
    private LocalDate dateDebut;  // Date de début
    private LocalDate dateFin;  // Date de fin
    private String nomFichier; // Nom du fichier associé au document
    private MultipartFile fichier; // Fichier à uploader
    private byte[] contenu;

    // Ajout des identifiants pour les associations
    private Long voyageEtudeId;
    private Long subventionId;
    private Long candidatureId;
    private String cheminFichier;
}