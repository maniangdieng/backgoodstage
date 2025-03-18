package gestion_voyage.gestion_voyage.service;

import gestion_voyage.gestion_voyage.dto.CandidatureDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CandidatureService {

    // Créer une nouvelle candidature
    CandidatureDto createCandidature(CandidatureDto candidatureDto);

    // Lire une candidature par ID
    CandidatureDto getCandidatureById(Long id);

    // Lire toutes les candidatures avec pagination
    Page<CandidatureDto> getAllCandidatures(Pageable pageable);

    // Lire toutes les candidatures sans pagination (si nécessaire)
    List<CandidatureDto> getAllCandidatures();  // Optionnel

    // Mettre à jour une candidature existante
    CandidatureDto updateCandidature(Long id, CandidatureDto candidatureDto);

    // Supprimer une candidature par ID
    void deleteCandidature(Long id);

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
}