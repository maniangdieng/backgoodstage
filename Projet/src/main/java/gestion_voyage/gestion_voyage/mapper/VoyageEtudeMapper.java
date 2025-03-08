package gestion_voyage.gestion_voyage.mapper;
import gestion_voyage.gestion_voyage.dto.VoyageEtudeDto;
import gestion_voyage.gestion_voyage.entity.VoyageEtude;
import gestion_voyage.gestion_voyage.entity.Documents;
import gestion_voyage.gestion_voyage.entity.Candidature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class VoyageEtudeMapper {


    // Méthode pour convertir un DTO en entité
    public VoyageEtude toEntity(VoyageEtudeDto dto) {
        if (dto == null) {
            return null; // Gérer le cas où le DTO est nul
        }

        VoyageEtude voyage = new VoyageEtude();
        voyage.setId(dto.getId());
        voyage.setDateCreation(dto.getDateCreation());
        voyage.setAnnee(dto.getAnnee());
        voyage.setObservation(dto.getObservation());
        voyage.setDateVoyage(dto.getDateVoyage());
        voyage.setDateRetour(dto.getDateRetour());
        voyage.setStatut(dto.getStatut());
        voyage.setSession(dto.getSession());

        // Convertir la liste des IDs en entités Documents
        if (dto.getDocumentIds() != null) {
            List<Documents> documents = dto.getDocumentIds().stream()
                    .map(id -> {
                        Documents document = new Documents();
                        document.setId(id);
                        return document;
                    })
                    .collect(Collectors.toList());
            voyage.setDocuments(documents);
        }


        // Convertir l'ID de candidature en entité Candidature (relation OneToOne)
        if (dto.getCandidatureId() != null) {
            Candidature candidature = new Candidature();
            candidature.setId(dto.getCandidatureId());
            voyage.setCandidature(candidature); // Utilisation de la candidature unique
        }

        return voyage;
    }

    // Méthode pour convertir une entité en DTO
    public VoyageEtudeDto toDto(VoyageEtude entity) {
        if (entity == null) {
            return null; // Gérer le cas où l'entité est nulle
        }

        VoyageEtudeDto dto = new VoyageEtudeDto();
        dto.setId(entity.getId());
        dto.setDateCreation(entity.getDateCreation());
        dto.setAnnee(entity.getAnnee());
        dto.setObservation(entity.getObservation());
        dto.setDateVoyage(entity.getDateVoyage());
        dto.setDateRetour(entity.getDateRetour());
        dto.setStatut(entity.getStatut());
        dto.setSession(entity.getSession());

        // Convertir la liste des entités Documents en leurs IDs
        if (entity.getDocuments() != null) {
            List<Long> documentIds = entity.getDocuments().stream()
                    .map(Documents::getId) // Extraction des IDs de Documents
                    .collect(Collectors.toList());
            dto.setDocumentIds(documentIds);
        }

        // Convertir la candidature en ID (relation OneToOne)
        if (entity.getCandidature() != null) {
            dto.setCandidatureId(entity.getCandidature().getId());
        }

        return dto;
    }
}