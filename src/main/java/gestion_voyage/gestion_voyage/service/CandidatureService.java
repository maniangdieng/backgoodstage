package gestion_voyage.gestion_voyage.service;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing candidatures, including creation, validation, document management,
 * and arrete generation.
 */
public interface CandidatureService {

    /**
     * Creates a new candidature with the provided details and associated documents.
     * @param candidatureDto DTO containing candidature details and files
     * @return DTO of the created candidature
     * @throws RuntimeException if the cohorte or personnel is not found, or if eligibility checks fail
     */
    CandidatureDto createCandidature(CandidatureDto candidatureDto);

    /**
     * Retrieves a candidature by its ID, including associated documents.
     * @param id ID of the candidature
     * @return DTO of the candidature
     * @throws RuntimeException if the candidature is not found
     */
    CandidatureDto getCandidatureById(Long id);

    /**
     * Retrieves all candidatures with pagination.
     * @param pageable Pagination information
     * @return Page of candidature DTOs
     */
    Page<CandidatureDto> getAllCandidatures(Pageable pageable);

    /**
     * Retrieves all candidatures without pagination.
     * @return List of candidature DTOs
     */
    List<CandidatureDto> getAllCandidatures();

    /**
     * Updates a candidature with new details and files.
     * @param id ID of the candidature
     * @param candidatureDto DTO with updated details
     * @return DTO of the updated candidature
     * @throws RuntimeException if the candidature is not found
     */
    CandidatureDto updateCandidature(Long id, CandidatureDto candidatureDto);

    /**
     * Updates the comment of a candidature.
     * @param id ID of the candidature
     * @param commentaire New comment
     * @return DTO of the updated candidature
     * @throws RuntimeException if the candidature is not found
     */
    CandidatureDto updateCommentaire(Long id, String commentaire);

    /**
     * Deletes a candidature by its ID.
     * @param id ID of the candidature
     * @throws RuntimeException if the candidature is not found
     */
    void deleteCandidature(Long id);

    /**
     * Submits voyage report documents and updates the voyage status.
     * @param candidatureId ID of the candidature
     * @param fichiers Map of document types and their files
     * @throws RuntimeException if the candidature or voyage is not found, or if required files are missing
     */
    Map<String, String> submitRapportVoyage(Long candidatureId, Map<String, MultipartFile> fichiers); // Nouvelle signature
    /**
     * Updates the status of a voyage based on its start date.
     * @param voyageId ID of the voyage
     * @throws RuntimeException if the voyage is not found
     */
    void updateVoyageStatus(Long voyageId);

    /**
     * Deletes a document and its associated file.
     * @param documentId ID of the document
     * @throws RuntimeException if the document is not found
     */
    void deleteDocument(Long documentId);

    /**
     * Retrieves candidatures by status.
     * @param statut Status to filter by
     * @return List of candidature DTOs
     */
    List<CandidatureDto> getCandidaturesByStatut(String statut);

    /**
     * Retrieves candidatures by submission date.
     * @param dateDepot Submission date to filter by
     * @return List of candidature DTOs
     */
    List<CandidatureDto> getCandidaturesByDateDepot(LocalDate dateDepot);

    /**
     * Retrieves candidatures by destination.
     * @param destination Destination to filter by
     * @return List of candidature DTOs
     */
    List<CandidatureDto> getCandidaturesByDestination(String destination);

    /**
     * Downloads a document file.
     * @param documentId ID of the document
     * @return Resource representing the file
     * @throws RuntimeException if the document or file is not found
     */
    Resource downloadDocument(Long documentId);

    /**
     * Retrieves the file path of a document.
     * @param documentId ID of the document
     * @return File path
     * @throws RuntimeException if the document is not found
     */
    String getDocumentUrl(Long documentId);

    /**
     * Validates a candidature and creates a corresponding VoyageEtude.
     * @param candidatureId ID of the candidature
     * @throws RuntimeException if the candidature is not found or not in EN_ATTENTE status
     */
    void validateCandidature(Long candidatureId);

    /**
     * Retrieves candidatures by personnel ID.
     * @param personnelId ID of the personnel
     * @return List of candidature DTOs
     */
    List<CandidatureDto> getCandidaturesByUtilisateur(Long personnelId);

    /**
     * Updates the status of a candidature based on its start date.
     * @param candidatureId ID of the candidature
     * @throws RuntimeException if the candidature is not found
     */
    void updateCandidatureStatus(Long candidatureId);

    /**
     * Generates an arrete (decision document) for a candidature.
     * @param candidatureId ID of the candidature
     * @throws RuntimeException if the candidature is not found or PDF generation fails
     */
    void etablirArrete(Long candidatureId);

    /**
     * Checks if an arrete exists for a candidature.
     * @param candidatureId ID of the candidature
     * @return true if an arrete exists, false otherwise
     */
    boolean checkArreteExiste(Long candidatureId);

    /**
     * Downloads an arrete file for a candidature.
     * @param candidatureId ID of the candidature
     * @return Resource representing the arrete file
     * @throws RuntimeException if the arrete or file is not found
     */
    Resource downloadArrete(Long candidatureId);

    /**
     * Generates a collective arrete for multiple candidatures.
     * @param candidatureIds IDs of the candidatures
     * @return Byte array of the PDF content
     * @throws RuntimeException if any candidature is invalid or PDF generation fails
     */
    byte[] etablirArreteCollectif(List<Long> candidatureIds);

    /**
     * Retrieves validated candidatures without an arrete.
     * @return List of candidature DTOs
     */
    List<CandidatureDto> getCandidaturesValidesSansArrete();

    /**
     * Updates the status of all voyages based on their start dates.
     * Scheduled to run daily at midnight.
     */
    void updateAllVoyageStatuses();

    /**
     * Retrieves the last relevant document from a completed voyage for a given personnel.
     * @param personnelId ID of the personnel
     * @param typeDocument Type of document to retrieve (e.g., rapportVoyage, carteEmbarquement)
     * @return DocumentsDto of the last document, or null if none found
     */
    DocumentsDto getLastVoyageDocument(Long personnelId, String typeDocument);
}