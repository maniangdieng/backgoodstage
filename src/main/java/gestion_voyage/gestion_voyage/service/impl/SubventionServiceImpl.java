package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.SubventionDto;
import gestion_voyage.gestion_voyage.entity.Subvention;
import gestion_voyage.gestion_voyage.exception.ResourceNotFoundException;
import gestion_voyage.gestion_voyage.mapper.SubventionMapper;
import gestion_voyage.gestion_voyage.repository.SubventionRepository;
import gestion_voyage.gestion_voyage.service.SubventionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Data
@Service
@AllArgsConstructor


public class SubventionServiceImpl implements SubventionService {

    @Autowired
    private final SubventionRepository repository;
    private final SubventionMapper mapper;

    @Override
    public SubventionDto create(SubventionDto subventionDto) {
        // Mapper le DTO vers l'entité Subvention et sauvegarder
        Subvention subvention = mapper.mapToSubvention(subventionDto);
        Subvention savedSubvention = repository.save(subvention);
        return mapper.mapToSubventionDto(savedSubvention);
    }

    @Override
    public SubventionDto getById(Long id) {
        // Récupérer la subvention par ID, ou lancer une exception si non trouvée
        Subvention subvention = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subvention", "id", id));
        return mapper.mapToSubventionDto(subvention);
    }

    @Override
    public List<SubventionDto> getAll() {
        // Récupérer toutes les subventions et les mapper vers DTO
        List<Subvention> subventions = repository.findAll();
        return subventions.stream()
                .map(mapper::mapToSubventionDto)
                .toList();
    }

    @Override
    public SubventionDto update(Long id, SubventionDto subventionDto) {
        // Vérifier si la subvention existe avant de mettre à jour
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Subvention", "id", id);
        }
        Subvention subvention = mapper.mapToSubvention(subventionDto);
        subvention.setId(id);
        Subvention updatedSubvention = repository.save(subvention);
        return mapper.mapToSubventionDto(updatedSubvention);
    }

    @Override
    public SubventionDto partialUpdate(Long id, Map<String, Object> updates) {
        // Récupérer la subvention par ID ou lancer une exception si non trouvée
        Subvention subvention = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subvention", "id", id));

        // Mettre à jour les champs spécifiés dans la map
        updates.forEach((key, value) -> {
            switch (key) {
                case "typeActivite" -> {
                    if (value instanceof String) {
                        subvention.setTypeActivite((String) value);
                    }
                }
                case "montant" -> {
                    if (value instanceof Double) {
                        subvention.setMontant((Double) value);
                    }
                }
                case "statut" -> {
                    if (value instanceof String) {
                        subvention.setStatut((String) value);
                    }
                }
            }
        });

        Subvention updatedSubvention = repository.save(subvention);
        return mapper.mapToSubventionDto(updatedSubvention);
    }

    @Override
    public void delete(Long id) {
        // Vérifier si la subvention existe avant de la supprimer
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Subvention", "id", id);
        }
        repository.deleteById(id);
    }
}
