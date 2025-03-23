package gestion_voyage.gestion_voyage.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.dto.VoyageEtudeDto;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.entity.Documents;
import gestion_voyage.gestion_voyage.entity.Personnel;
import gestion_voyage.gestion_voyage.repository.CandidatureRepository;
import gestion_voyage.gestion_voyage.repository.CohorteRepository;
import gestion_voyage.gestion_voyage.repository.DocumentsRepository;
import gestion_voyage.gestion_voyage.repository.PersonnelRepository;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import gestion_voyage.gestion_voyage.service.VoyageEtudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
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

  @Autowired
  private VoyageEtudeService voyageEtudeService;


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

    // Mettre à jour le statut de tous les documents associés à la candidature
    List<Documents> documents = documentsRepository.findByCandidatureId(candidatureId);
    for (Documents document : documents) {
      document.setStatut("VALIDÉ");
      documentsRepository.save(document);
    }

    // Vérifier si un voyage existe déjà pour cette candidature
    if (candidature.getVoyageEtude() != null) {
      throw new RuntimeException("Un voyage existe déjà pour cette candidature.");
    }

    // Mettre à jour le statut de la candidature
    candidature.setStatut("VALIDÉ");
    candidatureRepository.save(candidature);

    createVoyageEtudeFromCandidature(candidature);
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

  // Méthode pour créer un voyage d'étude à partir d'une candidature validée
  private void createVoyageEtudeFromCandidature(Candidature candidature) {
    VoyageEtudeDto voyageEtudeDto = new VoyageEtudeDto();
    voyageEtudeDto.setDateCreation(LocalDate.now()); // Date de création du voyage
    voyageEtudeDto.setAnnee(candidature.getCohorte().getAnnee()); // Année de la cohorte
    voyageEtudeDto.setObservation("Voyage créé automatiquement après validation de la candidature.");
    voyageEtudeDto.setDateVoyage(candidature.getDateDebut()); // Date de début du voyage
    voyageEtudeDto.setDateRetour(candidature.getDateFin()); // Date de retour du voyage
    voyageEtudeDto.setStatut("EN_ATTENTE"); // Statut initial du voyage
    voyageEtudeDto.setSession("Session " + LocalDate.now().getYear()); // Session du voyage

    // Créer le voyage d'étude
    voyageEtudeService.create(voyageEtudeDto);
  }

  // Établir un arrêté
  public void etablirArrete(Long candidatureId) {
    try {
      System.out.println("Tentative d'établir un arrêté pour la candidature : " + candidatureId);
      Candidature candidature = candidatureRepository.findById(candidatureId)
        .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

      // Générer le PDF (exemple simplifié)
      byte[] pdfContent = generateArretePdf(candidature);

      // Enregistrer le PDF dans la table des documents
      Documents document = new Documents();
      document.setCandidature(candidature);
      document.setNomFichier("arrete_" + candidatureId + ".pdf");
      document.setContenu(pdfContent);
      document.setTypeDocument("ARRETE");
      document.setStatut("EN_ATTENTE"); // Définir un statut par défaut
      documentsRepository.save(document);
      System.out.println("Arrêté enregistré avec succès pour la candidature : " + candidatureId);
    } catch (Exception e) {
      System.err.println("Erreur lors de l'établissement de l'arrêté : " + e.getMessage());
      throw new RuntimeException("Erreur lors de l'établissement de l'arrêté : " + e.getMessage());
    }
  }  // Vérifier si un arrêté existe
  public boolean checkArreteExiste(Long candidatureId) {
    return documentsRepository.existsByCandidatureIdAndTypeDocument(candidatureId, "ARRETE");
  }

  // Télécharger l'arrêté
  public Resource downloadArrete(Long candidatureId) {
    Documents document = documentsRepository.findByCandidatureIdAndTypeDocument(candidatureId, "ARRETE")
      .orElseThrow(() -> new RuntimeException("Arrêté non trouvé"));

    return new ByteArrayResource(document.getContenu());
  }

  // Générer le PDF (exemple simplifié)


  private byte[] generateArretePdf(Candidature candidature) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Document document = new Document();

    try {
      // Initialiser le PDF
      PdfWriter.getInstance(document, outputStream);
      document.open();

      // Charger le logo
      Image logo = Image.getInstance("src/main/resources/logouasz.png"); // Chemin vers le logo
      logo.scaleToFit(100, 100); // Redimensionner le logo
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




}

