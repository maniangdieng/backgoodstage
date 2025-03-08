package gestion_voyage.gestion_voyage.mapper;

import gestion_voyage.gestion_voyage.dto.CohorteDto;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import gestion_voyage.gestion_voyage.entity.Personnel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;



@Component
public class CohorteMapper {


    // Méthode pour convertir une entité Cohorte en CohorteDto
    public CohorteDto toDto(Cohorte cohorte) {
        return Optional.ofNullable(cohorte)
                .map(c -> new CohorteDto(
                        c.getId(),
                        c.getAnnee(),
                        c.getDateOuverture(),
                        c.getDateSemiCloture(),
                        c.getDateClotureDef(),
                        Optional.ofNullable(c.getCandidatures())
                                .orElse(Collections.emptySet()) // Évite le NullPointerException
                                .stream().map(Candidature::getId).collect(Collectors.toSet()),
                        Optional.ofNullable(c.getPersonnels())
                                .orElse(Collections.emptySet()) // Évite le NullPointerException
                                .stream().map(Personnel::getId).collect(Collectors.toSet())
                ))
                .orElse(null);
    }

    // Méthode pour convertir un CohorteDto en entité Cohorte
    public Cohorte toEntity(CohorteDto dto) {
        return Optional.ofNullable(dto)
                .map(cDto -> {
                    Cohorte cohorte = new Cohorte();
                    cohorte.setId(cDto.getId());
                    cohorte.setAnnee(cDto.getAnnee());
                    cohorte.setDateOuverture(cDto.getDateOuverture());
                    cohorte.setDateSemiCloture(cDto.getDateSemiCloture());
                    cohorte.setDateClotureDef(cDto.getDateClotureDef());

                    // Récupération des entités Candidature et Personnel
                    cohorte.setCandidatures(fetchCandidaturesByIds(cDto.getCandidatureIds()));
                    cohorte.setPersonnels(fetchPersonnelsByIds(cDto.getPersonnelIds()));
                    return cohorte;
                })
                .orElse(null);
    }

    // Méthode fictive pour récupérer les entités Candidature par leurs IDs
    private Set<Candidature> fetchCandidaturesByIds(Set<Long> ids) {
        // Implémentez la logique pour récupérer les entités Candidature par leurs IDs
        return null; // Remplacez par votre implémentation
    }

    // Méthode fictive pour récupérer les entités Personnel par leurs IDs
    private Set<Personnel> fetchPersonnelsByIds(Set<Long> ids) {
        // Implémentez la logique pour récupérer les entités Personnel par leurs IDs
        return null; // Remplacez par votre implémentation
    }
}