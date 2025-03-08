package gestion_voyage.gestion_voyage.mapper;

import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.dto.SubventionDto;
import gestion_voyage.gestion_voyage.entity.Subvention;
import gestion_voyage.gestion_voyage.entity.Documents; // Assurez-vous d'importer l'entité Documents
import gestion_voyage.gestion_voyage.entity.Personnel; // Assurez-vous d'importer l'entité Personnel
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component

public class SubventionMapper {


    // Mapper SubventionDto vers l'entité Subvention
    public Subvention mapToSubvention(SubventionDto dto) {
        Subvention subvention = new Subvention();
        subvention.setId(dto.getId());
        subvention.setTypeActivite(dto.getTypeActivite());
        subvention.setMontant(dto.getMontant());
        subvention.setStatut(dto.getStatut());

        // Mapper les documents s'ils sont disponibles
        if (dto.getDocuments() != null) {
            List<Documents> documents = dto.getDocuments().stream()
                    .map(this::mapToDocuments) // Supposant une méthode pour mapper DocumentsDto vers Documents
                    .collect(Collectors.toList());
            subvention.setDocuments(documents);
        }

        // Définir le personnel s'il y a un personnelId disponible
        if (dto.getPersonnelId() != null) {
            Personnel personnel = new Personnel();
            personnel.setId(dto.getPersonnelId());
            subvention.setPersonnel(personnel);
        }

        return subvention;
    }

    // Mapper l'entité Subvention vers SubventionDto
    public SubventionDto mapToSubventionDto(Subvention entity) {
        SubventionDto dto = new SubventionDto();
        dto.setId(entity.getId());
        dto.setTypeActivite(entity.getTypeActivite());
        dto.setMontant(entity.getMontant());
        dto.setStatut(entity.getStatut());

        // Mapper les documents s'ils sont disponibles
        if (entity.getDocuments() != null) {
            List<DocumentsDto> documentsDto = entity.getDocuments().stream()
                    .map(this::mapToDocumentsDto) // Supposant une méthode pour mapper Documents vers DocumentsDto
                    .collect(Collectors.toList());
            dto.setDocuments(documentsDto);
        }

        // Définir le personnelId
        if (entity.getPersonnel() != null) {
            dto.setPersonnelId(entity.getPersonnel().getId());
        }

        return dto;
    }

    // Méthode de placeholder pour mapper Documents vers DocumentsDto
    private Documents mapToDocuments(DocumentsDto dto) {
        // Implémentez la logique de mapping ici
        return new Documents(); // Remplacez par la logique de mapping réelle
    }

    // Méthode de placeholder pour mapper DocumentsDto vers Documents
    private DocumentsDto mapToDocumentsDto(Documents entity) {
        // Implémentez la logique de mapping ici
        return new DocumentsDto(); // Remplacez par la logique de mapping réelle
    }
}