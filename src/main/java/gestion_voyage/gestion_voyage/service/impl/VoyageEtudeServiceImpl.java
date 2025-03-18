package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.VoyageEtudeDto;
import gestion_voyage.gestion_voyage.entity.VoyageEtude;
import gestion_voyage.gestion_voyage.mapper.VoyageEtudeMapper;
import gestion_voyage.gestion_voyage.repository.VoyageEtudeRepository;
import gestion_voyage.gestion_voyage.service.VoyageEtudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoyageEtudeServiceImpl implements VoyageEtudeService {

    private final VoyageEtudeRepository voyageEtudeRepository;
    private final VoyageEtudeMapper voyageEtudeMapper;

    @Autowired
    public VoyageEtudeServiceImpl(VoyageEtudeRepository voyageEtudeRepository, VoyageEtudeMapper voyageEtudeMapper) {
        this.voyageEtudeRepository = voyageEtudeRepository;
        this.voyageEtudeMapper = voyageEtudeMapper;
    }

    @Override
    public VoyageEtudeDto create(VoyageEtudeDto voyageEtudeDto) {
        // Vérifier si un voyage existe déjà pour cette candidature
        if (voyageEtudeDto.getCandidatureId() != null) {
            Optional<VoyageEtude> existingVoyage = voyageEtudeRepository.findByCandidatureId(voyageEtudeDto.getCandidatureId());
            if (existingVoyage.isPresent()) {
                throw new IllegalStateException("Un voyage existe déjà pour cette candidature.");
            }
        }

        // Validation avant la création
        validateVoyageEtude(voyageEtudeDto);

        // Mapper DTO vers Entity
        VoyageEtude voyage = voyageEtudeMapper.toEntity(voyageEtudeDto);

        // Sauvegarder le voyage d'étude
        VoyageEtude savedVoyage = voyageEtudeRepository.save(voyage);

        // Mapper Entity vers DTO
        return voyageEtudeMapper.toDto(savedVoyage);
    }

    @Override
    public List<VoyageEtudeDto> getAllVoyagesEtudes() {
        return voyageEtudeRepository.findAll().stream()
                .map(voyageEtudeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VoyageEtudeDto> getVoyageEtudeById(Long id) {
        return voyageEtudeRepository.findById(id)
                .map(voyageEtudeMapper::toDto);
    }

    @Override
    public Optional<VoyageEtudeDto> updateVoyageEtude(Long id, VoyageEtudeDto voyageEtudeDto) {
        return voyageEtudeRepository.findById(id)
                .map(existingVoyage -> {
                    // Mettre à jour les champs avec les valeurs du DTO
                    updateVoyageWithDto(existingVoyage, voyageEtudeDto);
                    VoyageEtude updatedVoyage = voyageEtudeRepository.save(existingVoyage);
                    return voyageEtudeMapper.toDto(updatedVoyage);
                });
    }

    @Override
    public boolean deleteVoyageEtude(Long id) {
        if (voyageEtudeRepository.existsById(id)) {
            voyageEtudeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Page<VoyageEtudeDto> getVoyagesEtudesByStatut(String statut, Pageable pageable) {
        return voyageEtudeRepository.findByStatut(statut, pageable)
                .map(voyageEtudeMapper::toDto);
    }

    @Override
    public Page<VoyageEtudeDto> getVoyagesEtudesByAnnee(Integer annee, Pageable pageable) {
        return voyageEtudeRepository.findByAnnee(annee, pageable)
                .map(voyageEtudeMapper::toDto);
    }

    // Méthode privée pour valider le DTO avant la création
    private void validateVoyageEtude(VoyageEtudeDto voyageEtudeDto) {
        if (voyageEtudeDto.getDateVoyage().isAfter(voyageEtudeDto.getDateRetour())) {
            throw new IllegalArgumentException("La date de voyage ne peut pas être postérieure à la date de retour.");
        }
        // Ajoute d'autres validations si nécessaire
    }

    // Méthode privée pour mettre à jour les champs de l'entité VoyageEtude avec les données du DTO
    private void updateVoyageWithDto(VoyageEtude voyage, VoyageEtudeDto dto) {
        voyage.setDateCreation(dto.getDateCreation());
        voyage.setAnnee(dto.getAnnee());
        voyage.setObservation(dto.getObservation());
        voyage.setDateVoyage(dto.getDateVoyage());
        voyage.setDateRetour(dto.getDateRetour());
        voyage.setStatut(dto.getStatut());
        voyage.setSession(dto.getSession());
    }

    @Override
    public void startVoyage(Long voyageId) {
        VoyageEtude voyage = voyageEtudeRepository.findById(voyageId)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé"));

        if (!"EN_ATTENTE".equals(voyage.getStatut())) {
            throw new IllegalStateException("Le voyage doit être en statut EN_ATTENTE pour être démarré.");
        }

        voyage.setStatut("EN_COURS");
        voyageEtudeRepository.save(voyage);
    }

    @Override
    public void endVoyage(Long voyageId) {
        VoyageEtude voyage = voyageEtudeRepository.findById(voyageId)
                .orElseThrow(() -> new RuntimeException("Voyage non trouvé"));

        if (!"EN_COURS".equals(voyage.getStatut())) {
            throw new IllegalStateException("Le voyage doit être en statut EN_COURS pour être terminé.");
        }

        voyage.setStatut("TERMINÉ");
        voyageEtudeRepository.save(voyage);
    }
}
