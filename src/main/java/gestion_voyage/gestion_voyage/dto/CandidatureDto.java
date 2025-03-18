package gestion_voyage.gestion_voyage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignorer les champs inconnus
public class CandidatureDto {

  private Long id;

  @NotNull(message = "La date de dépôt est obligatoire")
  @PastOrPresent(message = "La date de dépôt doit être dans le passé ou aujourd'hui")
  private LocalDate dateDepot;

  @NotEmpty(message = "Le statut est obligatoire")
  private String statut;

  @FutureOrPresent(message = "La date de début doit être dans le futur ou aujourd'hui")
  private LocalDate dateDebut;

  @FutureOrPresent(message = "La date de fin doit être dans le futur ou aujourd'hui")
  private LocalDate dateFin;

  @AssertTrue(message = "La date de fin doit être après la date de début")
  public boolean isDateFinAfterDateDebut() {
    return dateDebut == null || dateFin == null || dateFin.isAfter(dateDebut);
  }

  @NotEmpty(message = "La destination est obligatoire")
  private String destination;

  private String commentaire;

  @NotNull(message = "La cohorte est obligatoire")
  private Long cohorteId; // Ajout du champ cohorteId

  @NotNull(message = "Le personnel est obligatoire")
  private Long personnelId; // Ajout du champ personnelId

  // Ajout des champs pour le nom, prénom du personnel et l'année de la cohorte
  private String personnelNom;
  private String personnelPrenom;
  private Integer cohorteAnnee;

  // Utilisation d'une Map pour les fichiers
  private Map<String, MultipartFile> fichiers; // Changement ici

  // Relations avec d'autres DTOs
  private CohorteDto cohorte;
  private VoyageEtudeDto voyageEtude;

  @Size(min = 0, message = "La liste des documents ne doit pas être vide")
  private List<DocumentsDto> documents;

  private PersonnelDto personnel;

  // Ajout des dates de la cohorte
  private LocalDate dateOuvertureCohorte;
  private LocalDate dateClotureCohorte;

  // Champs pour les fichiers
  private MultipartFile arreteTitularisation; // Fichier pour l'arrêté de titularisation
  private MultipartFile justificatifPrecedentVoyage; // Fichier pour le justificatif de voyage précédent
  private String typeCandidature; // Ajoutez ce champ
}