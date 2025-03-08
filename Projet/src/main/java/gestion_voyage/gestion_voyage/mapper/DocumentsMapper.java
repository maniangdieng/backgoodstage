package gestion_voyage.gestion_voyage.mapper;
import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.entity.Documents;
import gestion_voyage.gestion_voyage.entity.VoyageEtude;
import gestion_voyage.gestion_voyage.entity.Subvention;
import gestion_voyage.gestion_voyage.entity.Candidature;

import org.springframework.stereotype.Component;

@Component

public class DocumentsMapper {


    // Convertir un DocumentsDto en entité Documents
    public Documents toEntity(DocumentsDto dto) {
        Documents documents = new Documents();
        documents.setId(dto.getId());
        documents.setStatut(dto.getStatut());
        documents.setDateDebut(dto.getDateDebut());
        documents.setDateFin(dto.getDateFin());
        documents.setNomFichier(dto.getNomFichier());

        // Gérer les relations avec VoyageEtude, Subvention, et Candidature
        if (dto.getVoyageEtudeId() != null) {
            VoyageEtude voyageEtude = new VoyageEtude();
            voyageEtude.setId(dto.getVoyageEtudeId());
            documents.setVoyageEtude(voyageEtude);
        }

        if (dto.getSubventionId() != null) {
            Subvention subvention = new Subvention();
            subvention.setId(dto.getSubventionId());
            documents.setSubvention(subvention);
        }

        if (dto.getCandidatureId() != null) {
            Candidature candidature = new Candidature();
            candidature.setId(dto.getCandidatureId());
            documents.setCandidature(candidature);
        }

        return documents;
    }

    // Convertir une entité Documents en DocumentsDto
    public DocumentsDto toDto(Documents documents) {
        DocumentsDto dto = new DocumentsDto();
        dto.setId(documents.getId());
        dto.setStatut(documents.getStatut());
        dto.setDateDebut(documents.getDateDebut());
        dto.setDateFin(documents.getDateFin());
        dto.setNomFichier(documents.getNomFichier());

        // Gérer les relations avec VoyageEtude, Subvention, et Candidature
        if (documents.getVoyageEtude() != null) {
            dto.setVoyageEtudeId(documents.getVoyageEtude().getId());
        }

        if (documents.getSubvention() != null) {
            dto.setSubventionId(documents.getSubvention().getId());
        }

        if (documents.getCandidature() != null) {
            dto.setCandidatureId(documents.getCandidature().getId());
        }

        return dto;
    }
}