package gestion_voyage.gestion_voyage.mapper;

import gestion_voyage.gestion_voyage.dto.PersonnelDto;
import gestion_voyage.gestion_voyage.entity.Personnel;
import gestion_voyage.gestion_voyage.entity.Subvention;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Responsabilite;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Component
public class PersonnelMapper extends UtilisateurMapper {

    public PersonnelDto toDto(Personnel personnel) {
        if (personnel == null) {
            return null;
        }

        PersonnelDto dto = new PersonnelDto();
        dto.setId(personnel.getId());
        dto.setMatricule(personnel.getMatricule());
        dto.setType(personnel.getType());

        // Mapping des identifiants de candidatures
        dto.setCandidatureIds(personnel.getCandidatures().stream()
                .map(Candidature::getId)
                .collect(Collectors.toSet()));

        // Mapping des identifiants de responsabilités
        dto.setResponsabiliteIds(personnel.getResponsabilites().stream()
                .map(Responsabilite::getId)
                .collect(Collectors.toSet()));

        dto.setSubventionId(personnel.getSubvention() != null ? personnel.getSubvention().getId() : null);
        dto.setNom(personnel.getNom());
        dto.setPrenom(personnel.getPrenom());
        dto.setEmail(personnel.getEmail());
        dto.setLogin(personnel.getLogin());
        dto.setTelephone(personnel.getTelephone());
        // On omet le mot de passe pour des raisons de sécurité
        return dto;
    }

    // Méthode pour mapper une liste de Personnel en une liste de PersonnelDto
    public List<PersonnelDto> toDto(List<Personnel> personnels) {
        if (personnels == null) {
            return null;
        }

        return personnels.stream()
                .map(this::toDto) // Utilisation de la méthode toDto pour chaque élément
                .collect(Collectors.toList());
    }

    public Personnel toEntity(PersonnelDto dto,
                              Subvention subvention,
                              Set<Candidature> candidatures,
                              Set<Responsabilite> responsabilites) {
        if (dto == null) {
            return null;
        }

        Personnel personnel = new Personnel();
        personnel.setId(dto.getId());
        personnel.setMatricule(dto.getMatricule());

        personnel.setType(dto.getType());

        // Mapping des candidatures et des responsabilités
        personnel.setCandidatures(candidatures != null ? candidatures : Collections.emptySet());
        personnel.setResponsabilites(responsabilites != null ? responsabilites : Collections.emptySet());

        personnel.setSubvention(subvention);
        personnel.setNom(dto.getNom());
        personnel.setPrenom(dto.getPrenom());
        personnel.setEmail(dto.getEmail());
        personnel.setLogin(dto.getLogin());
        personnel.setTelephone(dto.getTelephone());
        personnel.setMotDePasse(dto.getMotDePasse());

        return personnel;
    }
}
