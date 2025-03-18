package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.entity.Documents;
import gestion_voyage.gestion_voyage.entity.Personnel;
import gestion_voyage.gestion_voyage.repository.CandidatureRepository;
import gestion_voyage.gestion_voyage.repository.CohorteRepository;
import gestion_voyage.gestion_voyage.repository.DocumentsRepository;
import gestion_voyage.gestion_voyage.repository.PersonnelRepository;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private DocumentsRepository documentsRepository;

  @Autowired
  private CohorteRepository cohorteRepository;

  @Autowired
  private PersonnelRepository personnelRepository;

  // Chemin de stockage des fichiers
  private static final String UPLOAD_DIR = "C:\\uploads\\";

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

    // Déterminer si l'enseignant est nouveau ou ancien
    boolean isNouveau = isEnseignantNouveau(candidatureDto.getPersonnelId());

    // Vérifier les documents requis
    if (isNouveau) {
      if (candidatureDto.getFichiers() == null || !candidatureDto.getFichiers().containsKey("arreteTitularisation")) {
        throw new RuntimeException("Un arrêté de titularisation est requis pour les nouveaux enseignants.");
      }

    } else {
        if (candidatureDto.getFichiers() == null || !candidatureDto.getFichiers().containsKey("justificatifPrecedentVoyage")){
        throw new RuntimeException("Un justificatif du précédent voyage est requis pour les anciens enseignants.");
      }
    }

    // Mapper le DTO vers l'entité
    Candidature candidature = new Candidature();
    candidature.setDateDepot(candidatureDto.getDateDepot());
    candidature.setDateDebut(candidatureDto.getDateDebut());
    candidature.setDateFin(candidatureDto.getDateFin());
    candidature.setStatut(candidatureDto.getStatut() != null ? candidatureDto.getStatut() : "EN_ATTENTE"); // Valeur par défaut
    candidature.setDestination(candidatureDto.getDestination());
    candidature.setCohorte(cohorte);
    candidature.setPersonnel(personnel);

    // Enregistrer en base de données
    Candidature savedCandidature = candidatureRepository.save(candidature);

    // Gérer les fichiers
    if (candidatureDto.getFichiers() != null && !candidatureDto.getFichiers().isEmpty()) {
      for (Map.Entry<String, MultipartFile> entry : candidatureDto.getFichiers().entrySet()) {
        String typeDocument = entry.getKey();
        MultipartFile file = entry.getValue();
        saveDocument(file, typeDocument, savedCandidature);
      }
    }

    // Mapper l'entité sauvegardée vers le DTO pour la réponse
    return mapToDto(savedCandidature);
  }

  private boolean isEnseignantNouveau(Long personnelId) {
    // Vérifier si l'enseignant a déjà effectué un voyage validé
    return candidatureRepository.countByPersonnelIdAndStatut(personnelId, "Voyage terminé") == 0;
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

    if (!areDocumentsValides(candidatureId)) {
      throw new RuntimeException("Tous les documents requis doivent être validés.");
    }

    // Mettre à jour le statut de la candidature
    candidature.setStatut("VALIDÉ");
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

<<<<<<< HEAD
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
}
=======




  @Override
  public List<CandidatureDto> getCandidaturesByUtilisateur(Long personnelId) {
    // Récupérer les candidatures du personnel via son ID
    return candidatureRepository.findByPersonnelId(personnelId).stream()
      .map(this::convertToDto)
      .collect(Collectors.toList());
  }
  private CandidatureDto convertToDto(Candidature candidature) {
    CandidatureDto dto = new CandidatureDto();
    dto.setId(candidature.getId());
    dto.setDateDepot(candidature.getDateDepot());
    dto.setDestination(candidature.getDestination());
    dto.setStatut(candidature.getStatut());
    // Ajoutez d'autres champs si nécessaire
    return dto;
  }




}
>>>>>>> 78aa0ee93cdfb0f4afbac018816b6b49b4c95300
