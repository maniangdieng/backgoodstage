package gestion_voyage.gestion_voyage.dto;


import gestion_voyage.gestion_voyage.entity.Personnel;
import lombok.*;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SubventionDto {

    Long id;

    String typeActivite;

    Double montant;

    String statut;


    // Liste de DocumentDto pour représenter la relation avec Document
    List<DocumentsDto> documents;

    // ID du Personnel pour représenter la relation avec Personnel
    Long personnelId;

}



