package gestion_voyage.gestion_voyage.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.dto.VoyageEtudeDto;
import gestion_voyage.gestion_voyage.entity.*;
import gestion_voyage.gestion_voyage.mapper.VoyageEtudeMapper;
import gestion_voyage.gestion_voyage.repository.*;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import gestion_voyage.gestion_voyage.service.VoyageEtudeService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CandidatureService for managing candidatures, voyages, documents, and arretes.
 */
@Service
public class CandidatureServiceImpl implements CandidatureService {

  private static final Logger logger = LoggerFactory.getLogger(CandidatureServiceImpl.class);

  @Autowired
  private CandidatureRepository candidatureRepository;

  @Autowired
  private VoyageEtudeRepository voyageEtudeRepository;

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
    if (isNouveau) {
      if (candidatureDto.getFichiers() == null || !candidatureDto.getFichiers().containsKey("arreteTitularisation")) {
        throw new RuntimeException("Un arrêté de titularisation est requis pour les nouveaux enseignants.");
      }
    } else {
      // Pour les enseignants anciens, vérifier les documents existants ou nouveaux
      List<String> requiredDocs = List.of("carteEmbarquement", "rapportVoyage");
      List<Candidature> candidatures = candidatureRepository.findByPersonnelId(candidatureDto.getPersonnelId());
      Optional<Candidature> lastCompletedCandidature = candidatures.stream()
              .filter(c -> c.getVoyageEtude() != null && "TERMINÉ".equals(c.getVoyageEtude().getStatut()))
              .sorted((c1, c2) -> c2.getDateFin().compareTo(c1.getDateFin()))
              .findFirst();

      if (lastCompletedCandidature.isPresent()) {
        List<Documents> existingDocs = documentsRepository.findByCandidatureId(lastCompletedCandidature.get().getId());
        for (String docType : requiredDocs) {
          boolean hasExistingDoc = existingDocs.stream().anyMatch(doc -> docType.equals(doc.getTypeDocument()));
          boolean hasNewDoc = candidatureDto.getFichiers() != null && candidatureDto.getFichiers().containsKey(docType);
          if (!hasExistingDoc && !hasNewDoc) {
            throw new RuntimeException("Un " + docType + " du précédent voyage est requis pour les anciens enseignants.");
          }
        }
      } else {
        // Si aucun voyage terminé, exiger les nouveaux documents
        for (String docType : requiredDocs) {
          if (candidatureDto.getFichiers() == null || !candidatureDto.getFichiers().containsKey(docType)) {
            throw new RuntimeException("Un " + docType + " du précédent voyage est requis pour les anciens enseignants.");
          }
        }
      }
    }

    // Créer la candidature
    Candidature candidature = new Candidature();
    candidature.setDateDepot(candidatureDto.getDateDepot());
    candidature.setDateDebut(candidatureDto.getDateDebut());
    candidature.setDateFin(candidatureDto.getDateFin());
    candidature.setStatut("EN_ATTENTE");
    candidature.setDestination(candidatureDto.getDestination());
    candidature.setCohorte(cohorte);
    candidature.setPersonnel(personnel);

    Candidature savedCandidature = candidatureRepository.save(candidature);

    // Sauvegarder les nouveaux fichiers
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
    dto.setCommentaire(candidature.getCommentaire());
    dto.setCohorteId(candidature.getCohorte().getId());
    dto.setPersonnelId(candidature.getPersonnel().getId());
    dto.setPersonnelNom(candidature.getPersonnel().getNom());
    dto.setPersonnelPrenom(candidature.getPersonnel().getPrenom());
    dto.setCohorteAnnee(candidature.getCohorte().getAnnee());

    // Ajouter le mappage de voyageEtude
    if (candidature.getVoyageEtude() != null) {
      dto.setVoyageEtude(voyageEtudeMapper.toDto(candidature.getVoyageEtude()));
    }

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
            .map(this::mapToDto)
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

  /**
   * Updates the status of a candidature based on its start date.
   * If the current date is equal to or after the start date, the status changes to EN_COURS.
   * @param candidatureId ID of the candidature
   * @throws RuntimeException if the candidature is not found
   */
  @Override
  public void updateCandidatureStatus(Long candidatureId) {
    Candidature candidature = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

    LocalDate today = LocalDate.now();
    logger.info("Mise à jour statut candidature ID {}: Statut actuel={}, Date début={}",
            candidatureId, candidature.getStatut(), candidature.getDateDebut());

    if ("EN_ATTENTE".equals(candidature.getStatut()) &&
            (today.isEqual(candidature.getDateDebut()) || today.isAfter(candidature.getDateDebut()))) {
      candidature.setStatut("EN_COURS");
      candidatureRepository.save(candidature);
      logger.info("Candidature ID {} passée à EN_COURS", candidatureId);
    } else {
      logger.info("Aucun changement pour candidature ID {}: Condition non remplie", candidatureId);
    }
  }

  /**
   * Updates the status of a voyage based on its start date.
   * If the current date is equal to or after the voyage start date, the status changes to EN_COURS.
   * @param voyageId ID of the voyage
   * @throws RuntimeException if the voyage is not found
   */
  @Override
  public void updateVoyageStatus(Long voyageId) {
    VoyageEtude voyage = voyageEtudeRepository.findById(voyageId)
            .orElseThrow(() -> new RuntimeException("Voyage non trouvé"));

    LocalDate today = LocalDate.now();
    logger.info("Mise à jour statut voyage ID {}: Statut actuel={}, Date voyage={}",
            voyageId, voyage.getStatut(), voyage.getDateVoyage());

    if ("EN_ATTENTE".equals(voyage.getStatut()) &&
            (today.isEqual(voyage.getDateVoyage()) || today.isAfter(voyage.getDateVoyage()))) {
      voyage.setStatut("EN_COURS");
      voyageEtudeRepository.save(voyage);
      logger.info("Voyage ID {} passé à EN_COURS", voyageId);
    } else {
      logger.info("Aucun changement pour voyage ID {}: Condition non remplie", voyageId);
    }
  }

  /**
   * Submits voyage report documents and updates the voyage status to TERMINÉ if the return date is reached.
   * @param candidatureId ID of the candidature
   * @param fichiers Map of document types and their files
   * @throws RuntimeException if the candidature or voyage is not found, or if required files are missing
   */
  @Override
  public Map<String, String> submitRapportVoyage(Long candidatureId, Map<String, MultipartFile> fichiers) {
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
    logger.info("Voyage ID {} passé à TERMINÉ après soumission des justificatifs", voyage.getId());

    // Retourner une réponse JSON
    return Map.of("message", "Justificatifs et rapport soumis. Voyage terminé.");
  }
  /**
   * Updates the status of all voyages daily at midnight.
   * - Changes EN_ATTENTE to EN_COURS if the current date is equal to or after the voyage start date.
   * - Changes EN_COURS to TERMINÉ if the return date is passed and required documents are submitted.
   */
  @Scheduled(cron = "*/30 * * * * *")  // Toutes les 30 secondes
  public void updateAllVoyageStatuses() {
    logger.info("Début de la mise à jour automatique des statuts des voyages");
    List<VoyageEtude> voyages = voyageEtudeRepository.findAll();
    LocalDate today = LocalDate.now();

    for (VoyageEtude voyage : voyages) {
      logger.info("Vérification voyage ID {}: Statut={}, Date voyage={}, Date retour={}",
              voyage.getId(), voyage.getStatut(), voyage.getDateVoyage(), voyage.getDateRetour());

      if ("EN_ATTENTE".equals(voyage.getStatut()) &&
              (today.isEqual(voyage.getDateVoyage()) || today.isAfter(voyage.getDateVoyage()))) {
        voyage.setStatut("EN_COURS");
        voyageEtudeRepository.save(voyage);
        logger.info("Voyage ID {} passé à EN_COURS", voyage.getId());
      } else if ("EN_COURS".equals(voyage.getStatut()) &&
              today.isAfter(voyage.getDateRetour()) &&
              areDocumentsSubmittedForVoyage(voyage)) {
        voyage.setStatut("TERMINÉ");
        voyageEtudeRepository.save(voyage);
        logger.info("Voyage ID {} passé à TERMINÉ", voyage.getId());
      } else {
        logger.info("Aucun changement pour voyage ID {}", voyage.getId());
      }
    }
    logger.info("Fin de la mise à jour automatique des statuts des voyages");
  }

  /**
   * Checks if the required voyage documents (carteEmbarquement and rapportVoyage) are submitted.
   * @param voyage The voyage to check
   * @return true if both documents are present, false otherwise
   */
  private boolean areDocumentsSubmittedForVoyage(VoyageEtude voyage) {
    Candidature candidature = candidatureRepository.findByVoyageEtudeId(voyage.getId())
            .orElse(null);
    if (candidature == null) {
      logger.warn("Aucune candidature trouvée pour le voyage ID {}", voyage.getId());
      return false;
    }
    List<Documents> documents = documentsRepository.findByCandidatureId(candidature.getId());
    boolean hasCarteEmbarquement = documents.stream()
            .anyMatch(doc -> "carteEmbarquement".equals(doc.getTypeDocument()));
    boolean hasRapportVoyage = documents.stream()
            .anyMatch(doc -> "rapportVoyage".equals(doc.getTypeDocument()));
    logger.info("Voyage ID {}: carteEmbarquement={}, rapportVoyage={}",
            voyage.getId(), hasCarteEmbarquement, hasRapportVoyage);
    return hasCarteEmbarquement && hasRapportVoyage;
  }

  @Override
  public void etablirArrete(Long candidatureId) {
    try {
      logger.info("Tentative d'établir un arrêté pour la candidature : {}", candidatureId);
      Candidature candidature = candidatureRepository.findById(candidatureId)
              .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

      // Générer le PDF
      byte[] pdfContent = generateArretePdf(candidature);

      // Sauvegarder le fichier sur le disque
      String fileName = "arrete_" + candidatureId + "_" + System.currentTimeMillis() + ".pdf";
      String filePath = UPLOAD_DIR + fileName;

      // Créer le répertoire de stockage s'il n'existe pas
      File uploadDir = new File(UPLOAD_DIR);
      if (!uploadDir.exists()) {
        uploadDir.mkdirs();
      }

      // Écrire le contenu du PDF dans un fichier
      java.nio.file.Files.write(java.nio.file.Paths.get(filePath), pdfContent);

      // Enregistrer le document dans la base de données
      Documents document = new Documents();
      document.setCandidature(candidature);
      document.setNomFichier(fileName);
      document.setCheminFichier(filePath);
      document.setTypeDocument("ARRETE");
      document.setStatut("EN_ATTENTE");
      documentsRepository.save(document);

      logger.info("Arrêté enregistré avec succès pour la candidature : {}", candidatureId);
    } catch (Exception e) {
      logger.error("Erreur lors de l'établissement de l'arrêté : {}", e.getMessage());
      throw new RuntimeException("Erreur lors de l'établissement de l'arrêté : " + e.getMessage());
    }
  }

  @Override
  public DocumentsDto getLastVoyageDocument(Long personnelId, String typeDocument) {
    // Trouver la dernière candidature avec un voyage terminé
    List<Candidature> candidatures = candidatureRepository.findByPersonnelId(personnelId);
    Optional<Candidature> lastCompletedCandidature = candidatures.stream()
            .filter(c -> c.getVoyageEtude() != null && "TERMINÉ".equals(c.getVoyageEtude().getStatut()))
            .sorted((c1, c2) -> c2.getDateFin().compareTo(c1.getDateFin())) // Trier par date de fin (plus récent d'abord)
            .findFirst();

    if (lastCompletedCandidature.isEmpty()) {
      logger.info("Aucun voyage terminé trouvé pour personnelId={}", personnelId);
      return null;
    }

    // Récupérer le document du type spécifié
    List<Documents> documents = documentsRepository.findByCandidatureId(lastCompletedCandidature.get().getId());
    Optional<Documents> document = documents.stream()
            .filter(doc -> typeDocument.equals(doc.getTypeDocument()))
            .findFirst();

    if (document.isEmpty()) {
      logger.info("Aucun document de type {} trouvé pour candidatureId={}", typeDocument, lastCompletedCandidature.get().getId());
      return null;
    }

    return mapToDocumentDto(document.get());
  }

  @Override
  public boolean checkArreteExiste(Long candidatureId) {
    return documentsRepository.existsByCandidatureIdAndTypeDocument(candidatureId, "ARRETE");
  }

  @Override
  public Resource downloadArrete(Long candidatureId) {
    Documents document = documentsRepository.findByCandidatureIdAndTypeDocument(candidatureId, "ARRETE")
            .orElseThrow(() -> new RuntimeException("Arrêté non trouvé"));

    File file = new File(document.getCheminFichier());
    if (!file.exists()) {
      throw new RuntimeException("Fichier de l'arrêté non trouvé sur le disque.");
    }

    return new FileSystemResource(file);
  }

  private byte[] generateArretePdf(Candidature candidature) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Document document = new Document();

    try {
      // Initialiser le PDF
      PdfWriter.getInstance(document, outputStream);
      document.open();

      // Charger le logo
      Image logo = Image.getInstance("src/main/resources/logouasz.png");
      logo.scaleToFit(100, 100);
      logo.setAlignment(Element.ALIGN_CENTER);
      document.add(logo);

      // Ajouter un titre stylisé
      Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
      Paragraph title = new Paragraph("Université Assane Seck de Ziguinchor (UASZ)", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      document.add(title);

      // Ajouter un sous-titre
      Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
      Paragraph subtitle = new Paragraph("Direction des Ressources Humaines (DRH)", subtitleFont);
      subtitle.setAlignment(Element.ALIGN_CENTER);
      document.add(subtitle);

      // Ajouter une ligne vide pour l'espacement
      document.add(new Paragraph("\n"));

      // Ajouter la référence de l'arrêté
      Font referenceFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
      Paragraph reference = new Paragraph("Référence : Arrêté N° 2023/DRH/001", referenceFont);
      reference.setAlignment(Element.ALIGN_CENTER);
      document.add(reference);

      // Ajouter une ligne vide pour l'espacement
      document.add(new Paragraph("\n"));

      // Ajouter le texte de l'arrêté
      Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
      document.add(new Paragraph("Le Directeur des Ressources Humaines,", textFont));
      document.add(new Paragraph("\n"));

      document.add(new Paragraph("Vu le décret N° 2023-001 portant organisation des voyages d'études et missions des enseignants de l'Université Assane Seck de Ziguinchor ;", textFont));
      document.add(new Paragraph("Vu la demande de voyage d'études déposée par l'enseignant concerné ;", textFont));
      document.add(new Paragraph("Vu la liste des candidats sélectionnés pour les voyages d'études et missions approuvée par la Direction de la Recherche et de la Coopération (DRC) ;", textFont));
      document.add(new Paragraph("Vu la disponibilité des fonds alloués par la Direction des Finances et de la Comptabilité (DFC) ;", textFont));
      document.add(new Paragraph("\n"));

      document.add(new Paragraph("Arrête :", textFont));
      document.add(new Paragraph("\n"));

      // Ajouter les articles sous forme de tableau pour une meilleure présentation
      PdfPTable table = new PdfPTable(1);
      table.setWidthPercentage(100);

      PdfPCell cell = new PdfPCell(new Paragraph("Article 1 :", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Monsieur/Madame " + candidature.getPersonnel().getNom() + " " + candidature.getPersonnel().getPrenom() +
              ", enseignant(e) à l'Université Assane Seck de Ziguinchor, est autorisé(e) à effectuer un voyage d'études/mission à " +
              candidature.getDestination() + " du " + candidature.getDateDebut() + " au " + candidature.getDateFin() + ".", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Article 2 :", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Ce voyage s'inscrit dans le cadre de la cohorte " + candidature.getCohorte().getAnnee() +
              " pour l'année académique " + candidature.getCohorte().getAnnee() + ".", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Article 3 :", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Les frais de voyage, d'hébergement et de restauration seront pris en charge par l'Université Assane Seck de Ziguinchor, conformément aux dispositions budgétaires en vigueur.", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Article 4 :", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("À l'issue du voyage, l'enseignant(e) devra remettre un rapport détaillé de ses activités à la Direction de la Recherche et de la Coopération (DRC) dans un délai de 30 jours.", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Article 5 :", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      cell = new PdfPCell(new Paragraph("Le présent arrêté sera notifié à l'enseignant(e) concerné(e), à la Direction de la Recherche et de la Coopération (DRC), à la Direction des Finances et de la Comptabilité (DFC), et à la Direction Générale de l'Université.", textFont));
      cell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(cell);

      document.add(table);

      // Ajouter la date et la signature
      document.add(new Paragraph("\n"));
      document.add(new Paragraph("Fait à Ziguinchor, le " + LocalDate.now(), textFont));
      document.add(new Paragraph("Le Directeur des Ressources Humaines,", textFont));
      document.add(new Paragraph("Dr. Inconnu Inconnu", textFont));
      document.add(new Paragraph("Signature", textFont));

      document.close();
    } catch (DocumentException | IOException e) {
      throw new RuntimeException("Erreur lors de la génération du PDF : " + e.getMessage());
    }

    return outputStream.toByteArray();
  }

  @Override
  public byte[] etablirArreteCollectif(List<Long> candidatureIds) {
    // Vérifier que toutes les candidatures sont valides et sans arrêté
    List<Candidature> candidatures = candidatureRepository.findAllById(candidatureIds);

    if (candidatures.isEmpty()) {
      throw new RuntimeException("Aucune candidature sélectionnée");
    }

    for (Candidature c : candidatures) {
      if (!"VALIDÉ".equals(c.getStatut())) {
        throw new RuntimeException("La candidature " + c.getId() + " n'est pas validée");
      }
      if (checkArreteExiste(c.getId())) {
        throw new RuntimeException("La candidature " + c.getId() + " a déjà un arrêté");
      }
    }

    // Générer le PDF collectif
    byte[] pdfContent = generateArreteCollectifPdf(candidatures);

    // Sauvegarder le fichier sur le disque
    String fileName = "arrete_collectif_" + System.currentTimeMillis() + ".pdf";
    String filePath = UPLOAD_DIR + fileName;

    try {
      // Créer le répertoire de stockage s'il n'existe pas
      File uploadDir = new File(UPLOAD_DIR);
      if (!uploadDir.exists()) {
        uploadDir.mkdirs();
      }

      // Écrire le contenu du PDF dans un fichier
      java.nio.file.Files.write(java.nio.file.Paths.get(filePath), pdfContent);

      // Créer une entrée document pour chaque candidature
      for (Candidature candidature : candidatures) {
        Documents document = new Documents();
        document.setNomFichier(fileName);
        document.setCheminFichier(filePath);
        document.setTypeDocument("ARRETE_COLLECTIF");
        document.setStatut("VALIDÉ");
        document.setCandidature(candidature);

        if (candidature.getVoyageEtude() != null) {
          document.setVoyageEtude(candidature.getVoyageEtude());
        }

        documentsRepository.save(document);
      }

      return pdfContent;
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de l'enregistrement de l'arrêté collectif", e);
    }
  }

  private byte[] generateArreteCollectifPdf(List<Candidature> candidatures) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         PDDocument document = new PDDocument()) {

      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);
      PDPageContentStream contentStream = new PDPageContentStream(document, page);

      try {
        PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
        PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
        PDType1Font textFont = PDType1Font.HELVETICA;

        // En-tête
        PDImageXObject logo = PDImageXObject.createFromFile("src/main/resources/logouasz.png", document);
        contentStream.drawImage(logo, 250, 750, 100, 100);

        contentStream.beginText();
        contentStream.setFont(titleFont, 18);
        contentStream.newLineAtOffset(100, 750);
        contentStream.showText("Université Assane Seck de Ziguinchor (UASZ)");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(titleFont, 14);
        contentStream.newLineAtOffset(100, 720);
        contentStream.showText("Direction des Ressources Humaines (DRH)");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(headerFont, 12);
        contentStream.newLineAtOffset(100, 690);
        contentStream.showText("Référence : Arrêté N° 2023/DRH/002");
        contentStream.endText();

        // Corps de l'arrêté
        contentStream.beginText();
        contentStream.setFont(textFont, 12);
        contentStream.newLineAtOffset(100, 650);
        contentStream.showText("Le Directeur des Ressources Humaines,");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, 630);
        contentStream.showText("Vu le décret N° 2023-001 portant organisation des voyages d'études et missions des enseignants;");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, 610);
        contentStream.showText("Vu les demandes de voyages d'études déposées par les enseignants concernés;");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, 590);
        contentStream.showText("Vu la liste des candidats sélectionnés approuvée par la DRC;");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, 570);
        contentStream.showText("Vu la disponibilité des fonds alloués par la DFC;");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, 550);
        contentStream.showText("Arrête :");
        contentStream.endText();

        // Article 1 - Liste des bénéficiaires
        contentStream.beginText();
        contentStream.setFont(headerFont, 12);
        contentStream.newLineAtOffset(100, 520);
        contentStream.showText("Article 1 :");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(textFont, 12);
        contentStream.newLineAtOffset(100, 500);
        contentStream.showText("Les enseignants suivants sont autorisés à effectuer des voyages d'études/missions :");
        contentStream.endText();

        int yPosition = 480;
        for (Candidature c : candidatures) {
          contentStream.beginText();
          contentStream.setFont(textFont, 12);
          contentStream.newLineAtOffset(120, yPosition);
          contentStream.showText("- " + c.getPersonnel().getNom() + " " + c.getPersonnel().getPrenom() +
                  " : " + c.getDestination() +
                  " (" + formatDate(c.getDateDebut()) +
                  " - " + formatDate(c.getDateFin()) + ")");
          contentStream.endText();
          yPosition -= 20;

          if (yPosition < 100) {
            contentStream.close();
            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);
            contentStream = new PDPageContentStream(document, newPage); // Créer un nouveau content stream
            yPosition = 750;
          }
        }

        // Article 2 - Cadre du voyage
        yPosition -= 30;
        contentStream.beginText();
        contentStream.setFont(headerFont, 12);
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Article 2 :");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(textFont, 12);
        contentStream.newLineAtOffset(100, yPosition - 20);
        contentStream.showText("Ces voyages s'inscrivent dans le cadre de la cohorte " +
                candidatures.get(0).getCohorte().getAnnee() +
                " pour l'année académique " +
                candidatures.get(0).getCohorte().getAnnee() + ".");
        contentStream.endText();

        // Article 3 - Prise en charge financière
        yPosition -= 50;
        contentStream.beginText();
        contentStream.setFont(headerFont, 12);
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Article 3 :");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(textFont, 12);
        contentStream.newLineAtOffset(100, yPosition - 20);
        contentStream.showText("Les frais seront pris en charge conformément aux dispositions budgétaires en vigueur.");
        contentStream.endText();

        // Article 4 - Rapport de voyage
        yPosition -= 50;
        contentStream.beginText();
        contentStream.setFont(headerFont, 12);
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Article 4 :");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(textFont, 12);
        contentStream.newLineAtOffset(100, yPosition - 20);
        contentStream.showText("À l'issue de chaque voyage, les enseignants devront remettre un rapport détaillé à la DRC dans un délai de 30 jours.");
        contentStream.endText();

        // Article 5 - Notification
        yPosition -= 50;
        contentStream.beginText();
        contentStream.setFont(headerFont, 12);
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Article 5 :");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(textFont, 12);
        contentStream.newLineAtOffset(100, yPosition - 20);
        contentStream.showText("Le présent arrêté sera notifié aux parties concernées.");
        contentStream.endText();

        // Pied de page
        yPosition -= 60;
        contentStream.beginText();
        contentStream.setFont(textFont, 12);
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Fait à Ziguinchor, le " + formatDate(LocalDate.now()));
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition - 20);
        contentStream.showText("Le Directeur des Ressources Humaines,");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition - 60);
        contentStream.showText("Dr. [Nom du Directeur]");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition - 80);
        contentStream.showText("Signature");
        contentStream.endText();

        contentStream.close();
      } catch (IOException e) {
        throw new RuntimeException("Erreur lors de la génération du contenu PDF", e);
      }

      document.save(outputStream);
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Erreur lors de la génération du PDF", e);
    }
  }

  // Méthodes utilitaires
  private String formatDate(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
  }

  @Override
  public List<CandidatureDto> getCandidaturesValidesSansArrete() {
    return candidatureRepository.findByStatut("VALIDÉ").stream()
            .filter(c -> !documentsRepository.existsByCandidatureIdAndTypeDocumentIn(
                    c.getId(),
                    List.of("ARRETE", "ARRETE_COLLECTIF")))
            .map(this::mapToDto)
            .collect(Collectors.toList());
  }
}