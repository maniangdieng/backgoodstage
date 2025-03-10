package gestion_voyage.gestion_voyage.mapper;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Cohorte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CandidatureMapper {

    private final CohorteMapper cohorteMapper;
    private final VoyageEtudeMapper voyageEtudeMapper;
    private final PersonnelMapper personnelMapper;
    private final DocumentsMapper documentsMapper;

    @Autowired
    public CandidatureMapper(CohorteMapper cohorteMapper, VoyageEtudeMapper voyageEtudeMapper,
                             PersonnelMapper personnelMapper, DocumentsMapper documentsMapper) {
        this.cohorteMapper = cohorteMapper;
        this.voyageEtudeMapper = voyageEtudeMapper;
        this.personnelMapper = personnelMapper;
        this.documentsMapper = documentsMapper;
    }

    // Méthode pour transformer le DTO en entité
    public Candidature toEntity(CandidatureDto dto, Cohorte cohorte) {
        if (dto == null) return null;

        Candidature candidature = new Candidature();
        candidature.setId(dto.getId());
        candidature.setDateDepot(dto.getDateDepot());
        candidature.setStatut(dto.getStatut());
        candidature.setDateDebut(dto.getDateDebut());
        candidature.setDateFin(dto.getDateFin());
        candidature.setDestination(dto.getDestination());
        candidature.setCommentaire(dto.getCommentaire());

        // Mapping de la cohorte
        candidature.setCohorte(Optional.ofNullable(dto.getCohorte())
                .map(cohorteMapper::toEntity).orElse(null));

        // Mapping du voyage d'étude
        candidature.setVoyageEtude(Optional.ofNullable(dto.getVoyageEtude())
                .map(voyageEtudeMapper::toEntity).orElse(null));

        // Mapping des documents
        candidature.setDocuments(Optional.ofNullable(dto.getDocuments())
                .map(docs -> docs.stream()
                        .map(documentsMapper::toEntity)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList()));

        return candidature;
    }

    public void updateEntityFromDto(CandidatureDto dto, Candidature candidature, Cohorte cohorte) {
        if (dto == null || candidature == null) return;

        candidature.setDateDepot(dto.getDateDepot());
        candidature.setStatut(dto.getStatut());
        candidature.setDateDebut(dto.getDateDebut());
        candidature.setDateFin(dto.getDateFin());
        candidature.setDestination(dto.getDestination());
        candidature.setCommentaire(dto.getCommentaire());

        // Mise à jour de la cohorte
        candidature.setCohorte(Optional.ofNullable(dto.getCohorte())
                .map(cohorteMapper::toEntity).orElse(null));

        // Mise à jour du voyage d'étude
        candidature.setVoyageEtude(Optional.ofNullable(dto.getVoyageEtude())
                .map(voyageEtudeMapper::toEntity).orElse(null));

        // Mise à jour des documents
        candidature.setDocuments(Optional.ofNullable(dto.getDocuments())
                .map(docs -> docs.stream()
                        .map(documentsMapper::toEntity)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList()));
    }

    // Méthode pour transformer l'entité en DTO
    public CandidatureDto toDto(Candidature candidature) {
      if (candidature == null) return null;

      CandidatureDto dto = new CandidatureDto();
      dto.setId(candidature.getId());
      dto.setDateDepot(candidature.getDateDepot());
      dto.setStatut(candidature.getStatut());
      dto.setDateDebut(candidature.getDateDebut());
      dto.setDateFin(candidature.getDateFin());
      dto.setDestination(candidature.getDestination());
      dto.setCommentaire(candidature.getCommentaire());

      // Mapping de la cohorte
      if (candidature.getCohorte() != null) {
        dto.setCohorteId(candidature.getCohorte().getId()); // Ajout de l'ID de la cohorte
        dto.setCohorteAnnee(candidature.getCohorte().getAnnee()); // Ajout de l'année de la cohorte
      }

      // Mapping du personnel
      if (candidature.getPersonnel() != null) {
        dto.setPersonnelId(candidature.getPersonnel().getId()); // Ajout de l'ID du personnel
        dto.setPersonelMatricule(candidature.getPersonnel().getMatricule()); // Ajout du nom du personnel
      }

      // Mapping du voyage d'étude
      dto.setVoyageEtude(voyageEtudeMapper.toDto(candidature.getVoyageEtude()));

      // Mapping des documents
      dto.setDocuments(candidature.getDocuments().stream()
        .map(documentsMapper::toDto)
        .collect(Collectors.toList()));

      return dto;
    }
}
