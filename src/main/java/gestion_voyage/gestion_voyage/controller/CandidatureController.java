package gestion_voyage.gestion_voyage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.repository.CandidatureRepository;
import gestion_voyage.gestion_voyage.repository.CohorteRepository;
import gestion_voyage.gestion_voyage.service.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing candidatures, including creation, validation, document submission,
 * and arrete generation.
 */
@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    /**
     * Creates a new candidature with associated documents.
     * @param candidatureJson JSON string containing candidature details
     * @param arreteTitularisation Optional file for new teachers
     * @param justificatifPrecedentVoyage Optional file for experienced teachers
     * @return ResponseEntity with the created candidature DTO or an error message
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCandidature(
            @RequestPart("candidature") String candidatureJson,
            @RequestPart(value = "arreteTitularisation", required = false) MultipartFile arreteTitularisation,
            @RequestPart(value = "justificatifPrecedentVoyage", required = false) MultipartFile justificatifPrecedentVoyage) {
        try {
            // Convert JSON to CandidatureDto
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            CandidatureDto candidatureDto = objectMapper.readValue(candidatureJson, CandidatureDto.class);

            // Verify cohorte existence
            Optional<Cohorte> cohorte = cohorteRepository.findById(candidatureDto.getCohorteId());
            if (cohorte.isEmpty()) {
                return ResponseEntity.badRequest().body("La cohorte sélectionnée n'existe pas.");
            }

            // Verify deposit date within cohorte period
            LocalDate dateDepot = candidatureDto.getDateDepot();
            LocalDate dateOuverture = cohorte.get().getDateOuverture();
            LocalDate dateClotureDef = cohorte.get().getDateClotureDef();

            if (dateDepot.isBefore(dateOuverture)) {
                return ResponseEntity.badRequest().body("La date de dépôt est antérieure à la date d'ouverture de la cohorte.");
            }
            if (dateDepot.isAfter(dateClotureDef)) {
                return ResponseEntity.badRequest().body("La date de dépôt est postérieure à la date de clôture définitive de la cohorte.");
            }

            // Map files to DTO
            Map<String, MultipartFile> fichiersMap = new HashMap<>();
            if (arreteTitularisation != null) {
                fichiersMap.put("arreteTitularisation", arreteTitularisation);
            }
            if (justificatifPrecedentVoyage != null) {
                fichiersMap.put("justificatifPrecedentVoyage", justificatifPrecedentVoyage);
            }
            candidatureDto.setFichiers(fichiersMap);

            // Create candidature
            CandidatureDto createdCandidature = candidatureService.createCandidature(candidatureDto);
            return ResponseEntity.ok(createdCandidature);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erreur lors de la conversion JSON : " + e.getMessage());
        }
    }

    /**
     * Retrieves a candidature by its ID.
     * @param id ID of the candidature
     * @return ResponseEntity with the candidature DTO
     * @throws RuntimeException if the candidature is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CandidatureDto> getCandidatureById(@PathVariable Long id) {
        CandidatureDto candidatureDto = candidatureService.getCandidatureById(id);
        return ResponseEntity.ok(candidatureDto);
    }

    /**
     * Retrieves all candidatures with pagination.
     * @param pageable Pagination information
     * @return ResponseEntity with a page of candidature DTOs
     */
    @GetMapping
    public ResponseEntity<Page<CandidatureDto>> getAllCandidatures(Pageable pageable) {
        Page<CandidatureDto> candidatures = candidatureService.getAllCandidatures(pageable);
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Retrieves all candidatures without pagination.
     * @return ResponseEntity with a list of candidature DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<List<CandidatureDto>> getAllCandidaturesNoPagination() {
        List<CandidatureDto> candidatures = candidatureService.getAllCandidatures();
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Updates an existing candidature with new details and files.
     * @param id ID of the candidature
     * @param candidatureJson JSON string with updated candidature details
     * @param files Optional list of files
     * @return ResponseEntity with the updated candidature DTO or an error message
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCandidature(
            @PathVariable Long id,
            @RequestPart("candidature") String candidatureJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            // Convert JSON to CandidatureDto
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            CandidatureDto candidatureDto = objectMapper.readValue(candidatureJson, CandidatureDto.class);

            // Verify candidature existence
            CandidatureDto existingCandidature = candidatureService.getCandidatureById(id);
            if (existingCandidature == null) {
                return ResponseEntity.notFound().build();
            }

            // Verify cohorte
            Optional<Cohorte> cohorte = cohorteRepository.findById(existingCandidature.getCohorteId());
            if (cohorte.isEmpty()) {
                return ResponseEntity.badRequest().body("Cohorte non trouvée.");
            }

            // Check if cohorte closing date has passed
            LocalDate aujourdHui = LocalDate.now();
            if (aujourdHui.isAfter(cohorte.get().getDateClotureDef())) {
                return ResponseEntity.badRequest().body("La date de clôture de la cohorte est passée. Modification impossible.");
            }

            // Add files to DTO if present
            if (files != null && !files.isEmpty()) {
                Map<String, MultipartFile> fichiersMap = new HashMap<>();
                for (MultipartFile file : files) {
                    fichiersMap.put(file.getOriginalFilename(), file);
                }
                candidatureDto.setFichiers(fichiersMap);
            }

            // Update candidature
            CandidatureDto updatedCandidature = candidatureService.updateCandidature(id, candidatureDto);
            return ResponseEntity.ok(updatedCandidature);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erreur lors de la conversion JSON : " + e.getMessage());
        }
    }

    /**
     * Deletes a candidature by its ID.
     * @param id ID of the candidature
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidature(@PathVariable Long id) {
        candidatureService.deleteCandidature(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves candidatures by status.
     * @param statut Status to filter by
     * @return ResponseEntity with a list of candidature DTOs
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByStatut(@PathVariable String statut) {
        List<CandidatureDto> candidatures = candidatureService.getCandidaturesByStatut(statut);
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Retrieves candidatures by submission date.
     * @param dateDepot Submission date to filter by
     * @return ResponseEntity with a list of candidature DTOs
     */
    @GetMapping("/dateDepot/{dateDepot}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByDateDepot(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepot) {
        List<CandidatureDto> candidatures = candidatureService.getCandidaturesByDateDepot(dateDepot);
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Retrieves candidatures by destination.
     * @param destination Destination to filter by
     * @return ResponseEntity with a list of candidature DTOs
     */
    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesByDestination(@PathVariable String destination) {
        List<CandidatureDto> candidatures = candidatureService.getCandidaturesByDestination(destination);
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Downloads a document file.
     * @param documentId ID of the document
     * @return ResponseEntity with the document resource
     */
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        Resource resource = candidatureService.downloadDocument(documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Retrieves the URL of a document.
     * @param documentId ID of the document
     * @return ResponseEntity with the document URL
     */
    @GetMapping("/documents/{documentId}/url")
    public ResponseEntity<String> getDocumentUrl(@PathVariable Long documentId) {
        String documentUrl = candidatureService.getDocumentUrl(documentId);
        return ResponseEntity.ok(documentUrl);
    }

    /**
     * Retrieves candidatures for a specific user.
     * @param userId ID of the user (personnel)
     * @return ResponseEntity with a list of candidature DTOs
     */
    @GetMapping("/mes-candidatures/{userId}")
    public ResponseEntity<List<CandidatureDto>> getMesCandidatures(@PathVariable Long userId) {
        List<CandidatureDto> candidatures = candidatureService.getCandidaturesByUtilisateur(userId);
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Validates a candidature and optionally adds a comment.
     * @param id ID of the candidature
     * @param commentaire Optional comment
     * @return ResponseEntity with a success message or an error
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validateCandidature(@PathVariable Long id, @RequestParam(required = false) String commentaire) {
        try {
            Candidature candidature = candidatureRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
            if (commentaire != null) {
                candidature.setCommentaire(commentaire);
                candidatureRepository.save(candidature);
            }
            candidatureService.validateCandidature(id);
            return ResponseEntity.ok("Candidature validée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Refuses a candidature and optionally adds a comment.
     * @param id ID of the candidature
     * @param commentaire Optional comment
     * @return ResponseEntity with a success message or an error
     */
    @PostMapping("/{id}/refuse")
    public ResponseEntity<?> refuseCandidature(@PathVariable Long id, @RequestParam(required = false) String commentaire) {
        try {
            Candidature candidature = candidatureRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
            if (commentaire != null) {
                candidature.setCommentaire(commentaire);
            }
            candidature.setStatut("REFUSÉ");
            candidatureRepository.save(candidature);
            return ResponseEntity.ok("Candidature refusée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Submits voyage report documents.
     * @param candidatureId ID of the candidature
     * @param carteEmbarquement Boarding pass file
     * @param justificatifDestination Optional destination justification file
     * @param rapportVoyage Voyage report file
     * @return ResponseEntity with a success message or an error
     */
    @PostMapping("/rapport-voyage")
    public ResponseEntity<Map<String, String>> submitRapportVoyage(
            @RequestParam("candidatureId") Long candidatureId,
            @RequestParam("carteEmbarquement") MultipartFile carteEmbarquement,
            @RequestParam(value = "justificatifDestination", required = false) MultipartFile justificatifDestination,
            @RequestParam("rapportVoyage") MultipartFile rapportVoyage) {
        Map<String, MultipartFile> fichiers = Map.of(
                        "carteEmbarquement", carteEmbarquement,
                        "justificatifDestination", justificatifDestination,
                        "rapportVoyage", rapportVoyage
                ).entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, String> response = candidatureService.submitRapportVoyage(candidatureId, fichiers);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the status of a specific voyage.
     * @param voyageId ID of the voyage
     * @return ResponseEntity with a success message or an error
     */
    @PostMapping("/voyage/{voyageId}/update-status")
    public ResponseEntity<?> updateVoyageStatus(@PathVariable Long voyageId) {
        try {
            candidatureService.updateVoyageStatus(voyageId);
            return ResponseEntity.ok("Statut du voyage mis à jour.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Updates the status of all voyages (for testing the scheduled task).
     * @return ResponseEntity with a success message or an error
     */
    @PostMapping("/test-update-voyage-statuses")
    public ResponseEntity<?> testUpdateVoyageStatuses() {
        try {
            candidatureService.updateAllVoyageStatuses();
            return ResponseEntity.ok("Mise à jour des statuts des voyages effectuée.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Generates an arrete for a candidature.
     * @param id ID of the candidature
     * @return ResponseEntity with a success message or an error
     */
    @PostMapping("/{id}/etablir-arrete")
    public ResponseEntity<?> etablirArrete(@PathVariable Long id) {
        try {
            candidatureService.etablirArrete(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Arrêté établi avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Checks if an arrete exists for a candidature.
     * @param id ID of the candidature
     * @return ResponseEntity with a boolean indicating arrete existence
     */
    @GetMapping("/{id}/arrete-existe")
    public ResponseEntity<Boolean> checkArreteExiste(@PathVariable Long id) {
        boolean arreteExiste = candidatureService.checkArreteExiste(id);
        return ResponseEntity.ok(arreteExiste);
    }

    /**
     * Downloads an arrete file for a candidature.
     * @param id ID of the candidature
     * @return ResponseEntity with the arrete file
     */
    @GetMapping("/{id}/download-arrete")
    public ResponseEntity<Resource> downloadArrete(@PathVariable Long id) {
        Resource resource = candidatureService.downloadArrete(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"arrete_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    /**
     * Generates a collective arrete for multiple candidatures.
     * @param candidatureIds IDs of the candidatures
     * @return ResponseEntity with the PDF content or an error
     */
    @PostMapping("/etablir-arrete-collectif")
    public ResponseEntity<?> etablirArreteCollectif(@RequestBody List<Long> candidatureIds) {
        try {
            byte[] pdfContent = candidatureService.etablirArreteCollectif(candidatureIds);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("arrete_collectif.pdf").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Retrieves validated candidatures without an arrete.
     * @return ResponseEntity with a list of candidature DTOs
     */
    @GetMapping("/candidatures-valides-sans-arrete")
    public ResponseEntity<List<CandidatureDto>> getCandidaturesValidesSansArrete() {
        List<CandidatureDto> candidatures = candidatureService.getCandidaturesValidesSansArrete();
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Retrieves the last voyage document of a specified type for a user.
     * @param personnelId ID of the personnel
     * @param typeDocument Type of document (e.g., rapportVoyage, carteEmbarquement)
     * @return ResponseEntity with the document DTO or not found status
     */
    @GetMapping("/last-voyage-document/{personnelId}/{typeDocument}")
    public ResponseEntity<DocumentsDto> getLastVoyageDocument(
            @PathVariable Long personnelId,
            @PathVariable String typeDocument) {
        DocumentsDto document = candidatureService.getLastVoyageDocument(personnelId, typeDocument);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(document);
    }
}