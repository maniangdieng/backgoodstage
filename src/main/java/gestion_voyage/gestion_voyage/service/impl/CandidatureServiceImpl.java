package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.entity.Personnel;
import gestion_voyage.gestion_voyage.repository.CandidatureRepository;
import gestion_voyage.gestion_voyage.repository.CohorteRepository;
import gestion_voyage.gestion_voyage.repository.PersonnelRepository;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidatureServiceImpl implements CandidatureService {

  @Autowired
  private CandidatureRepository candidatureRepository;

  @Autowired
  private CohorteRepository cohorteRepository;

  @Autowired
  private PersonnelRepository personnelRepository;


  @Override
  public CandidatureDto createCandidature(CandidatureDto candidatureDto) {
    // Vérifier que la cohorte et le personnel existent
    Cohorte cohorte = cohorteRepository.findById(candidatureDto.getCohorteId())
      .orElseThrow(() -> new RuntimeException("Cohorte non trouvée"));
    Personnel personnel = personnelRepository.findById(candidatureDto.getPersonnelId())
      .orElseThrow(() -> new RuntimeException("Personnel non trouvé"));

    // Vérifier que la date de dépôt est dans l'intervalle de la cohorte
    LocalDate dateDepot = candidatureDto.getDateDepot();
    LocalDate dateOuverture = cohorte.getDateOuverture();
    LocalDate dateCloture = cohorte.getDateClotureDef();

    if (dateDepot.isBefore(dateOuverture)) {
      throw new RuntimeException("La date de dépôt est antérieure à la date d'ouverture de la cohorte.");
    }
    if (dateDepot.isAfter(dateCloture)) {
      throw new RuntimeException("La date de dépôt est postérieure à la date de clôture de la cohorte.");
    }

    // Mapper le DTO vers l'entité
    Candidature candidature = new Candidature();
    candidature.setDateDepot(candidatureDto.getDateDepot());
    candidature.setDateDebut(candidatureDto.getDateDebut());
    candidature.setDateFin(candidatureDto.getDateFin());
    candidature.setStatut(candidatureDto.getStatut());
    candidature.setDestination(candidatureDto.getDestination());
    candidature.setCohorte(cohorte);
    candidature.setPersonnel(personnel);

    // Enregistrer en base de données
    Candidature savedCandidature = candidatureRepository.save(candidature);

    // Mapper l'entité sauvegardée vers le DTO pour la réponse
    return mapToDto(savedCandidature);
  }
  @Override
  public CandidatureDto getCandidatureById(Long id) {
    Candidature candidature = candidatureRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
    return mapToDto(candidature);
  }

  @Override
  public Page<CandidatureDto> getAllCandidatures(Pageable pageable) {
    return candidatureRepository.findAll(pageable)
      .map(this::mapToDto);
  }

  @Override
  public List<CandidatureDto> getAllCandidatures() {
    return candidatureRepository.findAll()
      .stream()
      .map(this::mapToDto)
      .collect(Collectors.toList());
  }

  @Override
  public CandidatureDto updateCandidature(Long id, CandidatureDto candidatureDto) {
    Candidature candidature = candidatureRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

    // Mettre à jour les champs
    candidature.setDateDepot(candidatureDto.getDateDepot());
    candidature.setDateDebut(candidatureDto.getDateDebut());
    candidature.setDateFin(candidatureDto.getDateFin());
    candidature.setStatut(candidatureDto.getStatut());
    candidature.setDestination(candidatureDto.getDestination());

    // Enregistrer les modifications
    Candidature updatedCandidature = candidatureRepository.save(candidature);
    return mapToDto(updatedCandidature);
  }

  @Override
  public void deleteCandidature(Long id) {
    candidatureRepository.deleteById(id);
  }

  @Override
  public List<CandidatureDto> getCandidaturesByStatut(String statut) {
    return candidatureRepository.findByStatut(statut)
      .stream()
      .map(this::mapToDto)
      .collect(Collectors.toList());
  }

  @Override
  public List<CandidatureDto> getCandidaturesByDateDepot(LocalDate dateDepot) {
    return candidatureRepository.findByDateDepot(dateDepot)
      .stream()
      .map(this::mapToDto)
      .collect(Collectors.toList());
  }

  @Override
  public List<CandidatureDto> getCandidaturesByDestination(String destination) {
    return candidatureRepository.findByDestination(destination)
      .stream()
      .map(this::mapToDto)
      .collect(Collectors.toList());
  }

  // Méthode utilitaire pour mapper une entité Candidature vers un DTO
  private CandidatureDto mapToDto(Candidature candidature) {
    CandidatureDto dto = new CandidatureDto();
    dto.setId(candidature.getId());
    dto.setDateDepot(candidature.getDateDepot());
    dto.setDateDebut(candidature.getDateDebut());
    dto.setDateFin(candidature.getDateFin());
    dto.setStatut(candidature.getStatut());
    dto.setDestination(candidature.getDestination());
    dto.setCohorteId(candidature.getCohorte().getId());
    dto.setPersonnelId(candidature.getPersonnel().getId());

    // Ajout des informations supplémentaires
    dto.setPersonnelNom(candidature.getPersonnel().getNom());
    dto.setPersonnelPrenom(candidature.getPersonnel().getPrenom());
    dto.setCohorteAnnee(candidature.getCohorte().getAnnee());

    return dto;
  }
}
