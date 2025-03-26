package gestion_voyage.gestion_voyage.service;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CandidatureService {

    // Créer une nouvelle candidature
    CandidatureDto createCandidature(CandidatureDto candidatureDto);

    // Lire une candidature par ID
    CandidatureDto getCandidatureById(Long id);

    // Lire toutes les candidatures avec pagination
    Page<CandidatureDto> getAllCandidatures(Pageable pageable);

    // Lire toutes les candidatures sans pagination
    List<CandidatureDto> getAllCandidatures();

    // Mettre à jour une candidature existante
    CandidatureDto updateCandidature(Long id, CandidatureDto candidatureDto);

    // Mettre à jour le commentaire d'une candidature
    CandidatureDto updateCommentaire(Long id, String commentaire);

    // Supprimer une candidature par ID
    void deleteCandidature(Long id);

    // Soumettre un rapport de voyage
    void submitRapportVoyage(Long candidatureId, Map<String, MultipartFile> fichiers);

    // Mettre à jour le statut d'un voyage
    void updateVoyageStatus(Long voyageId);

    // Supprimer un document
    void deleteDocument(Long documentId);

    // Rechercher des candidatures par statut
    List<CandidatureDto> getCandidaturesByStatut(String statut);

    // Rechercher des candidatures par date de dépôt
    List<CandidatureDto> getCandidaturesByDateDepot(LocalDate dateDepot);

    // Rechercher des candidatures par destination
    List<CandidatureDto> getCandidaturesByDestination(String destination);

    // Télécharger un document
    Resource downloadDocument(Long documentId);

    // Obtenir l'URL d'un document
    String getDocumentUrl(Long documentId);

    // Valider une candidature
    void validateCandidature(Long candidatureId);

    // Obtenir les candidatures d'un utilisateur
    List<CandidatureDto> getCandidaturesByUtilisateur(Long personnelId);

    // Mettre à jour le statut d'une candidature
    void updateCandidatureStatus(Long candidatureId);

    // Établir un arrêté pour une candidature
    void etablirArrete(Long candidatureId);

    // Vérifier si un arrêté existe pour une candidature
    boolean checkArreteExiste(Long candidatureId);

    // Télécharger un arrêté
    Resource downloadArrete(Long candidatureId);



}
