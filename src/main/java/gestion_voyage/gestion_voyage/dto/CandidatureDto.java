package gestion_voyage.gestion_voyage.dto;
import jakarta.validation.constraints.*;
import lombok.*;

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

    // Relations avec d'autres DTOs
    CohorteDto cohorte;
    VoyageEtudeDto voyageEtude;

    @Size(min = 0, message = "La liste des documents ne doit pas être vide")
    private List<DocumentsDto> documents;

    PersonnelDto personnel;


}