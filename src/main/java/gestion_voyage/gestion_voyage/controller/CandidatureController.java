  package gestion_voyage.gestion_voyage.controller;


  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
  import gestion_voyage.gestion_voyage.dto.CandidatureDto;
  import gestion_voyage.gestion_voyage.entity.Cohorte;
  import gestion_voyage.gestion_voyage.entity.Personnel;
  import gestion_voyage.gestion_voyage.entity.Utilisateur;
  import gestion_voyage.gestion_voyage.repository.CohorteRepository;
  import gestion_voyage.gestion_voyage.repository.PersonnelRepository;
  import gestion_voyage.gestion_voyage.service.CandidatureService;
  import jakarta.servlet.http.HttpSession;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.core.io.Resource;
  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.Pageable;
  import org.springframework.http.HttpHeaders;
  import org.springframework.http.MediaType;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;
  import org.springframework.format.annotation.DateTimeFormat;
  import org.springframework.web.multipart.MultipartFile;

  import java.io.IOException;
  import java.time.LocalDate;
  import java.util.HashMap;
  import java.util.List;
  import java.util.Map;
  import java.util.Optional;
  import java.util.stream.Collectors;

  import com.fasterxml.jackson.databind.ObjectMapper;

  import static org.hibernate.query.sqm.tree.SqmNode.log;


  @RestController
  @RequestMapping("/api/candidatures")

  public class CandidatureController {
    @Autowired
    private CandidatureService candidatureService;
    @Autowired
    private CohorteRepository cohorteRepository;

    // Créer une nouvelle candidature


      @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
      public ResponseEntity<?> createCandidature(
              @RequestPart("candidature") String candidatureJson,
              @RequestPart(value = "arreteTitularisation", required = false) MultipartFile arreteTitularisation,
              @RequestPart(value = "justificatifPrecedentVoyage", required = false) MultipartFile justificatifPrecedentVoyage) {
          try {
              // Convert the JSON string to a CandidatureDto object
              ObjectMapper objectMapper = new ObjectMapper();
              objectMapper.registerModule(new JavaTimeModule());
              CandidatureDto candidatureDto = objectMapper.readValue(candidatureJson, CandidatureDto.class);

              // Check if the cohorte exists
              Optional<Cohorte> cohorte = cohorteRepository.findById(candidatureDto.getCohorteId());
              if (cohorte.isEmpty()) {
                  return ResponseEntity.badRequest().body("La cohorte sélectionnée n'existe pas.");
              }

              // Check if the deposit date is within the cohorte's period
              LocalDate dateDepot = candidatureDto.getDateDepot();
              LocalDate dateOuverture = cohorte.get().getDateOuverture();
              LocalDate dateClotureDef = cohorte.get().getDateClotureDef();

              if (dateDepot.isBefore(dateOuverture)) {
                  return ResponseEntity.badRequest().body("La date de dépôt est antérieure à la date d'ouverture de la cohorte.");
              }
              if (dateDepot.isAfter(dateClotureDef)) {
                  return ResponseEntity.badRequest().body("La date de dépôt est postérieure à la date de clôture définitive de la cohorte.");
              }

              // Map the files into a Map
              Map<String, MultipartFile> fichiersMap = new HashMap<>();
              if (arreteTitularisation != null) {
                  fichiersMap.put("arreteTitularisation", arreteTitularisation);
              }
              if (justificatifPrecedentVoyage != null) {
                  fichiersMap.put("justificatifPrecedentVoyage", justificatifPrecedentVoyage);
              }

              // Add the files to the DTO
              candidatureDto.setFichiers(fichiersMap);

              // Create the candidature
              CandidatureDto createdCandidature = candidatureService.createCandidature(candidatureDto);
              return ResponseEntity.ok(createdCandidature);
          } catch (IOException e) {
              return ResponseEntity.badRequest().body("Erreur lors de la conversion JSON : " + e.getMessage());
          }
      }


    // Lire une candidature par ID
    @GetMapping("/{id}")
    public ResponseEntity<CandidatureDto> getCandidatureById(@PathVariable Long id) {
      CandidatureDto candidatureDto = candidatureService.getCandidatureById(id);
      return ResponseEntity.ok(candidatureDto);
    }

    // Lire toutes les candidatures avec pagination
    @GetMapping
    public ResponseEntity<Page<CandidatureDto>> getAllCandidatures(Pageable pageable) {
      Page<CandidatureDto> candidatures = candidatureService.getAllCandidatures(pageable);
      return ResponseEntity.ok(candidatures);
    }

    // Lire toutes les candidatures sans pagination
    @GetMapping("/all")
    public ResponseEntity<List<CandidatureDto>> getAllCandidaturesNoPagination() {
      List<CandidatureDto> candidatures = candidatureService.getAllCandidatures();
      return ResponseEntity.ok(candidatures);
    }

    // Mettre à jour une candidature existante
    // Dans CandidatureController
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCandidature(@PathVariable Long id, @RequestBody CandidatureDto candidatureDto) {
      // Récupérer la candidature existante
      CandidatureDto existingCandidature = candidatureService.getCandidatureById(id);
      if (existingCandidature == null) {
        return ResponseEntity.notFound().build();
      }

      // Récupérer la cohorte associée
      Optional<Cohorte> cohorte = cohorteRepository.findById(existingCandidature.getCohorteId());
      if (cohorte.isEmpty()) {
        return ResponseEntity.badRequest().body("Cohorte non trouvée.");
      }

      // Vérifier si la date de clôture est dépassée
      LocalDate aujourdHui = LocalDate.now();
      if (aujourdHui.isAfter(cohorte.get().getDateClotureDef())) {
        return ResponseEntity.badRequest().body("La date de clôture de la cohorte est passée. Modification impossible.");
      }

      // Mettre à jour la candidature
      CandidatureDto updatedCandidature = candidatureService.updateCandidature(id, candidatureDto);
      return ResponseEntity.ok(updatedCandidature);
    }

    // Supprimer une candidature par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidature(@PathVariable Long id) {
      candidatureService.deleteCandidature(id);
      return ResponseEntity.noContent().build();
    }

    // Rechercher des candidatures par statut
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByStatut(@PathVariable String statut) {
      List<CandidatureDto> candidatures = candidatureService.getCandidaturesByStatut(statut);
      return ResponseEntity.ok(candidatures);
    }

    // Rechercher des candidatures par date de dépôt
    @GetMapping("/dateDepot/{dateDepot}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByDateDepot(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepot) {
      List<CandidatureDto> candidatures = candidatureService.getCandidaturesByDateDepot(dateDepot);
      return ResponseEntity.ok(candidatures);
    }

    // Rechercher des candidatures par destination
    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByDestination(@PathVariable String destination) {
      List<CandidatureDto> candidatures = candidatureService.getCandidaturesByDestination(destination);
      return ResponseEntity.ok(candidatures);
    }

    // Endpoint pour télécharger un document
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
      Resource resource = candidatureService.downloadDocument(documentId);
      return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
    }

    @GetMapping("/documents/{documentId}/url")
    public ResponseEntity<String> getDocumentUrl(@PathVariable Long documentId) {
      String documentUrl = candidatureService.getDocumentUrl(documentId);
      return ResponseEntity.ok(documentUrl);
    }
    @Autowired
    private PersonnelRepository personnelRepository;
    @GetMapping("/mes-candidatures/{userId}")
    public ResponseEntity<List<CandidatureDto>> getMesCandidatures(@PathVariable Long userId) {
      List<CandidatureDto> candidatures = candidatureService.getCandidaturesByUtilisateur(userId);
      return ResponseEntity.ok(candidatures);
    }
    }




