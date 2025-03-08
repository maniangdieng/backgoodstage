package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Cohorte; // Assurez-vous que cette classe existe
import gestion_voyage.gestion_voyage.mapper.CandidatureMapper;
import gestion_voyage.gestion_voyage.repository.CandidatureRepository;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidatureServiceImpl implements CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final CandidatureMapper candidatureMapper;

    @Autowired
    public CandidatureServiceImpl(CandidatureRepository candidatureRepository, CandidatureMapper candidatureMapper) {
        this.candidatureRepository = candidatureRepository;
        this.candidatureMapper = candidatureMapper;
    }

    @Override
    @Transactional
    public CandidatureDto createCandidature(CandidatureDto candidatureDto) {
        if (candidatureDto == null) {
            throw new IllegalArgumentException("CandidatureDto ne peut pas être null");
        }

        // Récupérer la cohorte si nécessaire (remplacez cette partie par une récupération réelle)
        Cohorte cohorte = null; // Remplacez cela par une logique de récupération de Cohorte si nécessaire

        // Mappage de l'entité
        Candidature candidature = candidatureMapper.toEntity(candidatureDto, cohorte);

        // Sauvegarde de la candidature
        Candidature savedCandidature = candidatureRepository.save(candidature);

        // Retourner le DTO
        return candidatureMapper.toDto(savedCandidature);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatureDto getCandidatureById(Long id) {
        return candidatureRepository.findById(id)
                .map(candidatureMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidatureDto> getAllCandidatures(Pageable pageable) {
        return candidatureRepository.findAll(pageable)
                .map(candidatureMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDto> getAllCandidatures() {
        return candidatureRepository.findAll().stream()
                .map(candidatureMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CandidatureDto updateCandidature(Long id, CandidatureDto candidatureDto) {
        if (candidatureDto == null) {
            throw new IllegalArgumentException("CandidatureDto ne peut pas être null");
        }

        // Récupérer l'entité existante
        Candidature existingCandidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID : " + id));

        // Récupérer la cohorte si nécessaire (remplacez cette partie par une récupération réelle)
        Cohorte cohorte = null; // Remplacez cela par une logique de récupération de Cohorte si nécessaire

        // Mettre à jour l'entité avec les données du DTO
        candidatureMapper.updateEntityFromDto(candidatureDto, existingCandidature, cohorte);

        // Sauvegarder l'entité mise à jour
        Candidature updatedCandidature = candidatureRepository.save(existingCandidature);

        // Retourner le DTO mis à jour
        return candidatureMapper.toDto(updatedCandidature);
    }

    @Override
    @Transactional
    public void deleteCandidature(Long id) {
        if (!candidatureRepository.existsById(id)) {
            throw new RuntimeException("Candidature non trouvée avec l'ID : " + id);
        }
        candidatureRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDto> getCandidaturesByStatut(String statut) {
        return candidatureRepository.findByStatut(statut).stream()
                .map(candidatureMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDto> getCandidaturesByDateDepot(LocalDate dateDepot) {
        return candidatureRepository.findByDateDepot(dateDepot).stream()
                .map(candidatureMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDto> getCandidaturesByDestination(String destination) {
        return candidatureRepository.findByDestination(destination).stream()
                .map(candidatureMapper::toDto)
                .collect(Collectors.toList());
    }
}
