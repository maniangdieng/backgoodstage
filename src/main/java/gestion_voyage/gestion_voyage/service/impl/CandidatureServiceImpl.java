package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.dto.VoyageEtudeDto;
import gestion_voyage.gestion_voyage.entity.*;
import gestion_voyage.gestion_voyage.mapper.VoyageEtudeMapper;
import gestion_voyage.gestion_voyage.repository.*;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import gestion_voyage.gestion_voyage.service.VoyageEtudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CandidatureServiceImpl implements CandidatureService {

  @Autowired
  private CandidatureRepository candidatureRepository;

  @Autowired
  private VoyageEtudeRepository  voyageEtudeRepository;


  @Autowired
  private VoyageEtudeMapper voyageEtudeMapper;

  @Autowired
  private DocumentsRepository documentsRepository;

  @Autowired
  private CohorteRepository cohorteRepository;

  @Autowired
  private PersonnelRepository personnelRepository;

  @Autowired
  private VoyageEtudeService voyageEtudeService;


  // Chemin de stockage des fichiers
  private static final String UPLOAD_DIR = "C:\\uploads\\";

  @Override
  public CandidatureDto createCandidature(CandidatureDto candidatureDto) {
    Cohorte cohorte = cohorteRepository.findById(candidatureDto.getCohorteId())
            .orElseThrow(() -> new RuntimeException("Cohorte non trouvée"));
    Personnel personnel = personnelRepository.findById(candidatureDto.getPersonnelId())
            .orElseThrow(() -> new RuntimeException("Personnel non trouvé"));

    // Vérifier les contraintes d'éligibilité
    checkEligibility(personnel, cohorte);

    // Vérifier la période de la cohorte
    LocalDate dateDepot = candidatureDto.getDateDepot();
    LocalDate dateOuverture = cohorte.getDateOuverture();
    LocalDate dateCloture = cohorte.getDateClotureDef();

    if (dateDepot.isBefore(dateOuverture) || dateDepot.isAfter(dateCloture)) {
      throw new RuntimeException("La date de dépôt est hors de la période de la cohorte.");
    }

    // Vérifier les documents requis
    boolean isNouveau = isEnseignantNouveau(candidatureDto.getPersonnelId());
    if (isNouveau && (candidatureDto.getFichiers() == null || !candidatureDto.getFichiers().containsKey("arreteTitularisation"))) {
      throw new RuntimeException("Un arrêté de titularisation est requis pour les nouveaux enseignants.");
    } else if (!isNouveau && (candidatureDto.getFichiers() == null || !candidatureDto.getFichiers().containsKey("justificatifPrecedentVoyage"))) {
      throw new RuntimeException("Un justificatif du précédent voyage est requis pour les anciens enseignants.");
    }

    // Créer la candidature
    Candidature candidature = new Candidature();
    candidature.setDateDepot(candidatureDto.getDateDepot());
    candidature.setDateDebut(candidatureDto.getDateDebut());
    candidature.setDateFin(candidatureDto.getDateFin());
    candidature.setStatut("EN_ATTENTE"); // Statut initial de la candidature
    candidature.setDestination(candidatureDto.getDestination());
    candidature.setCohorte(cohorte);
    candidature.setPersonnel(personnel);

    Candidature savedCandidature = candidatureRepository.save(candidature);

    // Sauvegarder les fichiers
    if (candidatureDto.getFichiers() != null && !candidatureDto.getFichiers().isEmpty()) {
      for (Map.Entry<String, MultipartFile> entry : candidatureDto.getFichiers().entrySet()) {
        saveDocument(entry.getValue(), entry.getKey(), savedCandidature);
      }
    }

    return mapToDto(savedCandidature);
  }

  private void checkEligibility(Personnel personnel, Cohorte nouvelleCohorte) {
    List<Candidature> candidatures = candidatureRepository.findByPersonnelId(personnel.getId());

    // Vérifier les candidatures en attente
    if (candidatures.stream().anyMatch(c -> "EN_ATTENTE".equals(c.getStatut()))) {
      throw new RuntimeException("Vous avez une candidature en attente. Nouvelle soumission interdite.");
    }

    // Vérifier les voyages en attente ou en cours
    for (Candidature c : candidatures) {
      if (c.getVoyageEtude() != null && ("EN_ATTENTE".equals(c.getVoyageEtude().getStatut()) || "EN_COURS".equals(c.getVoyageEtude().getStatut()))) {
        throw new RuntimeException("Vous avez un voyage en attente ou en cours. Nouvelle soumission interdite.");
      }
    }

    // Vérifier les cohortes consécutives
    int anneeNouvelleCohorte = nouvelleCohorte.getAnnee();
    for (Candidature c : candidatures) {
      int anneePrecedente = c.getCohorte().getAnnee();
      if (anneeNouvelleCohorte == anneePrecedente + 1) {
        throw new RuntimeException("Candidatures pour deux cohortes consécutives interdites. Attendez " + (anneePrecedente + 2) + ".");
      }
    }
  }

  private boolean isEnseignantNouveau(Long personnelId) {
    return candidatureRepository.findByPersonnelId(personnelId).stream()
            .noneMatch(c -> c.getVoyageEtude() != null && "TERMINÉ".equals(c.getVoyageEtude().getStatut()));
  }

  private boolean areDocumentsValides(Long candidatureId) {
    List<Documents> documents = documentsRepository.findByCandidatureId(candidatureId);
    return documents.stream()
            .allMatch(doc -> doc.getStatut().equals("VALIDÉ"));
  }

  private int hasVoyageEnCours(Long personnelId) {
    return candidatureRepository.existsByPersonnelIdAndStatutIn(
            personnelId,
            List.of("En attente de départ", "Voyage en cours")
    );
  }

  @Override
  public void validateCandidature(Long candidatureId) {
    Candidature candidature = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

    // Vérifier que la candidature est en attente
    if (!"EN_ATTENTE".equals(candidature.getStatut())) {
      throw new RuntimeException("La candidature doit être en attente pour être validée.");
    }

    // Valider les documents
    List<Documents> documents = documentsRepository.findByCandidatureId(candidatureId);
    for (Documents document : documents) {
      document.setStatut("VALIDÉ");
      documentsRepository.save(document);
    }

    // Mettre à jour le statut de la candidature
    candidature.setStatut("VALIDÉ");
    candidatureRepository.save(candidature);

    // Créer le voyage
    createVoyageEtudeFromCandidature(candidature);
  }

  private void createVoyageEtudeFromCandidature(Candidature candidature) {
    VoyageEtudeDto voyageEtudeDto = new VoyageEtudeDto();
    voyageEtudeDto.setDateCreation(LocalDate.now());
    voyageEtudeDto.setAnnee(candidature.getCohorte().getAnnee());
    voyageEtudeDto.setObservation("Voyage créé après validation de la candidature.");
    voyageEtudeDto.setDateVoyage(candidature.getDateDebut());
    voyageEtudeDto.setDateRetour(candidature.getDateFin());
    voyageEtudeDto.setStatut("EN_ATTENTE"); // Statut initial du voyage
    voyageEtudeDto.setSession("Session " + LocalDate.now().getYear());

    // Créer le voyage via le service
    VoyageEtudeDto createdVoyageDto = voyageEtudeService.create(voyageEtudeDto);

    // Convertir le DTO en entité
    VoyageEtude voyage = voyageEtudeMapper.toEntity(createdVoyageDto);

    // Lier le voyage à la candidature
    candidature.setVoyageEtude(voyage);
    candidatureRepository.save(candidature);
  }
  @Override
  public CandidatureDto getCandidatureById(Long id) {
    Candidature candidature = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

    // Récupérer les documents associés à la candidature
    List<Documents> documents = documentsRepository.findByCandidatureId(id);

    // Mapper la candidature et les documents vers le DTO
    CandidatureDto candidatureDto = mapToDto(candidature);
    candidatureDto.setDocuments(documents.stream()
            .map(this::mapToDocumentDto)
            .collect(Collectors.toList()));

    return candidatureDto;
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
    candidature.setCommentaire(candidatureDto.getCommentaire());

    // Gérer les fichiers
    if (candidatureDto.getFichiers() != null && !candidatureDto.getFichiers().isEmpty()) {
      for (Map.Entry<String, MultipartFile> entry : candidatureDto.getFichiers().entrySet()) {
        String typeDocument = entry.getKey();
        MultipartFile file = entry.getValue();
        saveDocument(file, typeDocument, candidature);
      }
    }

    // Enregistrer les modifications
    Candidature updatedCandidature = candidatureRepository.save(candidature);
    return mapToDto(updatedCandidature);
  }
  @Override
  public void deleteCandidature(Long id) {
    candidatureRepository.deleteById(id);
  }

  @Override
  public void deleteDocument(Long documentId) {
    Documents document = documentsRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document non trouvé"));

    // Supprimer le fichier du disque
    File file = new File(document.getCheminFichier());
    if (file.exists()) {
      file.delete();
    }

    // Supprimer le document de la base de données
    documentsRepository.delete(document);
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
    dto.setCommentaire(candidature.getCommentaire()); // Inclure le commentaire
    dto.setCohorteId(candidature.getCohorte().getId());
    dto.setPersonnelId(candidature.getPersonnel().getId());

    // Ajout des informations supplémentaires
    dto.setPersonnelNom(candidature.getPersonnel().getNom());
    dto.setPersonnelPrenom(candidature.getPersonnel().getPrenom());
    dto.setCohorteAnnee(candidature.getCohorte().getAnnee());

    return dto;
  }

  // Méthode utilitaire pour mapper une entité Documents vers un DocumentDto
  private DocumentsDto mapToDocumentDto(Documents document) {
    DocumentsDto dto = new DocumentsDto();
    dto.setId(document.getId());
    dto.setNomFichier(document.getNomFichier());
    dto.setCheminFichier(document.getCheminFichier());
    return dto;
  }

  @Override
  public Resource downloadDocument(Long documentId) {
    Documents document = documentsRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document non trouvé"));

    File file = new File(document.getCheminFichier());
    if (!file.exists()) {
      throw new RuntimeException("Fichier non trouvé");
    }

    return new FileSystemResource(file);
  }

  @Override
  public String getDocumentUrl(Long documentId) {
    Documents document = documentsRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document non trouvé"));

    // Retourner le chemin du fichier
    return document.getCheminFichier();
  }



  // Méthode pour sauvegarder un document
  private void saveDocument(MultipartFile file, String typeDocument, Candidature candidature) {
    try {
      // Créer le répertoire de stockage s'il n'existe pas
      File uploadDir = new File(UPLOAD_DIR);
      if (!uploadDir.exists()) {
        uploadDir.mkdirs();
      }

      // Générer un nom de fichier unique
      String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
      String filePath = UPLOAD_DIR + fileName;

      // Sauvegarder le fichier sur le disque
      file.transferTo(new File(filePath));

      // Enregistrer le document en base de données
      Documents document = new Documents();
      document.setNomFichier(file.getOriginalFilename());
      document.setCheminFichier(filePath);
      document.setTypeDocument(typeDocument);
      document.setStatut("EN_ATTENTE");
      document.setCandidature(candidature);
      documentsRepository.save(document);
    } catch (IOException e) {
      throw new RuntimeException("Erreur lors de la sauvegarde du fichier : " + e.getMessage());
    }
  }


  @Override
  public List<CandidatureDto> getCandidaturesByUtilisateur(Long personnelId) {
    return candidatureRepository.findByPersonnelId(personnelId).stream()
            .map(this::mapToDto) // Utiliser mapToDto au lieu de convertToDto
            .collect(Collectors.toList());
  }

  @Override
  public CandidatureDto updateCommentaire(Long id, String commentaire) {
    Candidature candidature = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

    candidature.setCommentaire(commentaire);
    Candidature updatedCandidature = candidatureRepository.save(candidature);
    return mapToDto(updatedCandidature);
  }

  public void updateCandidatureStatus(Long candidatureId) {
    Candidature candidature = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

    LocalDate today = LocalDate.now();
    if ("EN_ATTENTE".equals(candidature.getStatut()) && today.isEqual(candidature.getDateDebut())) {
      candidature.setStatut("EN_COURS");
      candidatureRepository.save(candidature);
    }



  }

  public void updateVoyageStatus(Long voyageId) {
    VoyageEtude voyage = voyageEtudeRepository.findById(voyageId)
            .orElseThrow(() -> new RuntimeException("Voyage non trouvé"));

    LocalDate today = LocalDate.now();
    if ("EN_ATTENTE".equals(voyage.getStatut()) && today.isEqual(voyage.getDateVoyage())) {
      voyage.setStatut("EN_COURS");
      voyageEtudeRepository.save(voyage);
    }
  }

  @Override
  public void submitRapportVoyage(Long candidatureId, Map<String, MultipartFile> fichiers) {
    Candidature candidature = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

    VoyageEtude voyage = candidature.getVoyageEtude();
    if (voyage == null || !"EN_COURS".equals(voyage.getStatut())) {
      throw new RuntimeException("Le voyage doit être en cours pour soumettre les justificatifs.");
    }
    if (LocalDate.now().isBefore(voyage.getDateRetour())) {
      throw new RuntimeException("Le voyage n’est pas encore terminé (date de retour non atteinte).");
    }

    // Vérifier les fichiers requis
    if (!fichiers.containsKey("carteEmbarquement") || !fichiers.containsKey("rapportVoyage")) {
      throw new RuntimeException("La carte d’embarquement et le rapport du voyage sont requis.");
    }

    // Enregistrer les fichiers
    for (Map.Entry<String, MultipartFile> entry : fichiers.entrySet()) {
      saveDocument(entry.getValue(), entry.getKey(), candidature);
    }

    // Mettre à jour le statut du voyage
    voyage.setStatut("TERMINÉ");
    voyageEtudeRepository.save(voyage);
  }

  @Scheduled(cron = "0 0 0 * * *") // Tous les jours à minuit
  public void updateAllVoyageStatuses() {
    List<VoyageEtude> voyages = voyageEtudeRepository.findAll();
    LocalDate today = LocalDate.now();

    for (VoyageEtude voyage : voyages) {
      if ("EN_ATTENTE".equals(voyage.getStatut()) && today.isEqual(voyage.getDateVoyage())) {
        voyage.setStatut("EN_COURS");
        voyageEtudeRepository.save(voyage);
      }
    }
  }


}

