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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class VoyageEtudeServiceImpl implements VoyageEtudeService{

    private final VoyageEtudeRepository voyageEtudeRepository;
    private final VoyageEtudeMapper voyageEtudeMapper;

    @Autowired
    public VoyageEtudeServiceImpl(VoyageEtudeRepository voyageEtudeRepository, VoyageEtudeMapper voyageEtudeMapper) {
        this.voyageEtudeRepository = voyageEtudeRepository;
        this.voyageEtudeMapper = voyageEtudeMapper;
    }

    @Override
    public VoyageEtudeDto create(VoyageEtudeDto voyageEtudeDto) {
        // Validation avant la création
        validateVoyageEtude(voyageEtudeDto);
        VoyageEtude voyage = voyageEtudeMapper.toEntity(voyageEtudeDto); // Mapper DTO vers Entity
        VoyageEtude savedVoyage = voyageEtudeRepository.save(voyage);
        return voyageEtudeMapper.toDto(savedVoyage); // Mapper Entity vers DTO
    }

    @Override
    public List<VoyageEtudeDto> getAllVoyagesEtudes() {
        return voyageEtudeRepository.findAll().stream()
                .map(voyageEtudeMapper::toDto) // Mapper chaque entity vers DTO
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VoyageEtudeDto> getVoyageEtudeById(Long id) {
        return voyageEtudeRepository.findById(id)
                .map(voyageEtudeMapper::toDto); // Mapper l'entity trouvée vers DTO
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
                .map(voyageEtudeMapper::toDto); // Mapper chaque page d'entity vers DTO
    }

    @Override
    public Page<VoyageEtudeDto> getVoyagesEtudesByAnnee(Integer annee, Pageable pageable) {
        return voyageEtudeRepository.findByAnnee(annee, pageable)
                .map(voyageEtudeMapper::toDto); // Mapper chaque page d'entity vers DTO
    }

    // Méthode privée pour valider le DTO avant la création
    private void validateVoyageEtude(VoyageEtudeDto voyageEtudeDto) {
        if (voyageEtudeDto.getDateVoyage().isAfter(voyageEtudeDto.getDateRetour())) {
            throw new IllegalArgumentException("La date de voyage ne peut pas être postérieure à la date de retour.");
        }
        // Ajoute d'autres validations si nécessaire (ex: champ obligatoire, etc.)
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
}
