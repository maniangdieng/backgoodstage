package gestion_voyage.gestion_voyage.dto;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoyageEtudeDto {

    Long id;
    LocalDate dateCreation;
    Integer annee;
    String observation;
    LocalDate dateVoyage;
    LocalDate dateRetour;
    String statut;
    String session;

    // Liste des IDs associés aux documents
    List<Long> documentIds;

    // ID unique de la candidature (relation OneToOne)
    Long candidatureId;

    // Si nécessaire, tu peux inclure les Dto complets au lieu des IDs
    // private List<DocumentDto> documents;
    // private CandidatureDto candidature;
}