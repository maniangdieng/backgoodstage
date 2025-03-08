package gestion_voyage.gestion_voyage.service;

import gestion_voyage.gestion_voyage.dto.SubventionDto;

import java.util.List;
import java.util.Map;

public interface SubventionService {

    // Crée une nouvelle subvention.
    SubventionDto create(SubventionDto subventionDto);

    // Récupère une subvention par son ID.
    SubventionDto getById(Long id);

    // Récupère toutes les subventions.
    List<SubventionDto> getAll();

    // Met à jour une subvention existante.
    SubventionDto update(Long id, SubventionDto subventionDto);

    // Effectue une mise à jour partielle d'une subvention existante.
    SubventionDto partialUpdate(Long id, Map<String, Object> updates);

    // Supprime une subvention par son ID.
    void delete(Long id);
}