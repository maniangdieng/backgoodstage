package gestion_voyage.gestion_voyage.mapper;

import gestion_voyage.gestion_voyage.dto.OrganisationDto;
import gestion_voyage.gestion_voyage.entity.Organisation;
import gestion_voyage.gestion_voyage.dto.ResponsabiliteDto;
import gestion_voyage.gestion_voyage.entity.Responsabilite;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component

public class OrganisationMapper {

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
    private ResponsabiliteDto toResponsabiliteDto(Responsabilite responsabilite) {
        if (responsabilite == null) {
            return null;
        }

        ResponsabiliteDto dto = new ResponsabiliteDto();
        dto.setId(responsabilite.getId());
        dto.setIdOrganisation(responsabilite.getOrganisation() != null ? responsabilite.getOrganisation().getIdOrganisation() : null);
        dto.setIdPersonnel(responsabilite.getPersonnel() != null ? responsabilite.getPersonnel().getId() : null);

        return dto;
    }

    // Méthode pour convertir un ResponsabiliteDto en entité Responsabilite
    private Responsabilite toResponsabiliteEntity(ResponsabiliteDto dto) {
        if (dto == null) {
            return null;
        }

        Responsabilite responsabilite = new Responsabilite();
        responsabilite.setId(dto.getId());

        // Assurez-vous de bien récupérer l'Organisation et le Personnel ici si nécessaire,
        // en passant par les services appropriés pour éviter les relations manquantes.

        return responsabilite;
    }
}