  package gestion_voyage.gestion_voyage.controller;


  import gestion_voyage.gestion_voyage.dto.CandidatureDto;
  import gestion_voyage.gestion_voyage.entity.Cohorte;
  import gestion_voyage.gestion_voyage.repository.CohorteRepository;
  import gestion_voyage.gestion_voyage.service.CandidatureService;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.Pageable;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;
  import org.springframework.format.annotation.DateTimeFormat;

  import java.time.LocalDate;
  import java.util.List;

  @RestController
  @RequestMapping("/api/candidatures")

  public class CandidatureController {
      @Autowired
      private CandidatureService candidatureService;
      @Autowired
      private CohorteRepository cohorteRepository;

      // Créer une nouvelle candidature
      @PostMapping
      public ResponseEntity<CandidatureDto> createCandidature(@RequestBody CandidatureDto candidatureDto) {
        if (candidatureDto.getStatut() == null || candidatureDto.getStatut().isEmpty()) {
          candidatureDto.setStatut("EN_ATTENTE"); // Valeur par défaut
        }
        // Récupérer la cohorte pour obtenir les dates
        Cohorte cohorte = cohorteRepository.findById(candidatureDto.getCohorteId())
          .orElseThrow(() -> new RuntimeException("Cohorte non trouvée"));

        // Ajouter les dates de la cohorte au DTO
        candidatureDto.setDateOuvertureCohorte(cohorte.getDateOuverture());
        candidatureDto.setDateClotureCohorte(cohorte.getDateClotureDef());
          CandidatureDto createdCandidature = candidatureService.createCandidature(candidatureDto);
          return ResponseEntity.ok(createdCandidature);
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
      @PutMapping("/{id}")
      public ResponseEntity<CandidatureDto> updateCandidature(@PathVariable Long id, @RequestBody CandidatureDto candidatureDto) {
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
  }
