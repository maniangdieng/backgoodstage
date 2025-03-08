package gestion_voyage.gestion_voyage.mapper;

import gestion_voyage.gestion_voyage.dto.OrganisationDto;
import gestion_voyage.gestion_voyage.dto.ResponsabiliteDto;
import gestion_voyage.gestion_voyage.entity.Organisation;
import gestion_voyage.gestion_voyage.entity.Responsabilite;
import gestion_voyage.gestion_voyage.entity.Personnel;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component

public class ResponsabiliteMapper {

    // Méthode pour convertir une entité Organisation en OrganisationDto
    public OrganisationDto toDto(Organisation organisation) {
        if (organisation == null) {
            return null;
        }

        OrganisationDto dto = new OrganisationDto();
        dto.setIdOrganisation(organisation.getIdOrganisation());
        dto.setOrganisation(organisation.getNomOrganisation());
        dto.setEmail(organisation.getEmail());
        dto.setTelephone(organisation.getTelephone());

        // Conversion des responsabilités en DTOs
        Set<ResponsabiliteDto> responsabiliteDtos = organisation.getResponsabilites()
                .stream()
                .map(this::toResponsabiliteDto)
                .collect(Collectors.toSet());

        dto.setResponsabilites(responsabiliteDtos);
        return dto;
    }

    // Méthode pour convertir un OrganisationDto en entité Organisation
    public Organisation toEntity(OrganisationDto dto) {
        if (dto == null) {
            return null;
        }

        Organisation organisation = new Organisation();
        organisation.setIdOrganisation(dto.getIdOrganisation());
        organisation.setNomOrganisation(dto.getOrganisation());
        organisation.setEmail(dto.getEmail());
        organisation.setTelephone(dto.getTelephone());

        // Conversion des DTOs de responsabilités en entités
        Set<Responsabilite> responsabilites = dto.getResponsabilites()
                .stream()
                .map(this::toResponsabiliteEntity)
                .collect(Collectors.toSet());

        organisation.setResponsabilites(responsabilites);
        return organisation;
    }

    // Méthode pour convertir une Responsabilite en ResponsabiliteDto
    public ResponsabiliteDto toResponsabiliteDto(Responsabilite responsabilite) {
        if (responsabilite == null) {
            return null;
        }

        ResponsabiliteDto dto = new ResponsabiliteDto();
        dto.setId(responsabilite.getId());
        dto.setLibelle(responsabilite.getLibelle());
        dto.setDateDebut(responsabilite.getDateDebut());
        dto.setDateFin(responsabilite.getDateFin());

        // Gestion des associations : Organisation et Personnel
        dto.setIdOrganisation(Optional.ofNullable(responsabilite.getOrganisation())
                .map(Organisation::getIdOrganisation)
                .orElse(null));
        dto.setIdPersonnel(Optional.ofNullable(responsabilite.getPersonnel())
                .map(Personnel::getId)
                .orElse(null));

        return dto;
    }

    // Méthode pour convertir un ResponsabiliteDto en entité Responsabilite
    public Responsabilite toResponsabiliteEntity(ResponsabiliteDto dto) {
        if (dto == null) {
            return null;
        }

        Responsabilite responsabilite = new Responsabilite();
        responsabilite.setId(dto.getId());
        responsabilite.setLibelle(dto.getLibelle());
        responsabilite.setDateDebut(dto.getDateDebut());
        responsabilite.setDateFin(dto.getDateFin());

        // Gestion des associations : Organisation et Personnel
        if (dto.getIdOrganisation() != null) {
            Organisation organisation = new Organisation();
            organisation.setIdOrganisation(dto.getIdOrganisation());
            responsabilite.setOrganisation(organisation);
        }

        if (dto.getIdPersonnel() != null) {
            Personnel personnel = new Personnel();
            personnel.setId(dto.getIdPersonnel());
            responsabilite.setPersonnel(personnel);
        }

        return responsabilite;
    }
}