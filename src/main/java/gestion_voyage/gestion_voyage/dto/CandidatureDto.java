package gestion_voyage.gestion_voyage.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidatureDto {

  Long id;

  @NotNull(message = "La date de dépôt est obligatoire")
  @PastOrPresent(message = "La date de dépôt doit être dans le passé ou aujourd'hui")
  LocalDate dateDepot;

  @NotEmpty(message = "Le statut est obligatoire")
  String statut;

  @FutureOrPresent(message = "La date de début doit être dans le futur ou aujourd'hui")
  LocalDate dateDebut;

  @FutureOrPresent(message = "La date de fin doit être dans le futur ou aujourd'hui")
  LocalDate dateFin;

  @AssertTrue(message = "La date de fin doit être après la date de début")
  public boolean isDateFinAfterDateDebut() {
    return dateDebut == null || dateFin == null || dateFin.isAfter(dateDebut);
  }

  @NotEmpty(message = "La destination est obligatoire")
  String destination;

  String commentaire;

  @NotNull(message = "La cohorte est obligatoire")
  private Long cohorteId; // Ajout du champ cohorteId

  @NotNull(message = "Le personnel est obligatoire")
  private Long personnelId; // Ajout du champ personnelId

  // Ajout des champs pour le nom, prénom du personnel et l'année de la cohorte
  private String personnelNom;
  private String personnelPrenom;
  private Integer cohorteAnnee;

  // Relations avec d'autres DTOs
  CohorteDto cohorte;
  VoyageEtudeDto voyageEtude;

  @Size(min = 0, message = "La liste des documents ne doit pas être vide")
  private List<DocumentsDto> documents;

  PersonnelDto personnel;

  // Ajout des dates de la cohorte
  private LocalDate dateOuvertureCohorte;
  private LocalDate dateClotureCohorte;
}

