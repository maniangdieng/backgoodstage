package gestion_voyage.gestion_voyage.service;

import gestion_voyage.gestion_voyage.dto.DocumentsDto;

import java.util.List;
public interface DocumentsService {

    // Crée un nouveau document
    DocumentsDto createDocument(DocumentsDto documentsDto);

    // Récupère un document par son ID
    DocumentsDto getDocumentById(Long id);

    // Récupère tous les documents
    List<DocumentsDto> getAllDocuments();

    // Met à jour un document existant
    DocumentsDto updateDocument(Long id, DocumentsDto documentsDto);

    // Supprime un document par son ID
    void deleteDocument(Long id);

    // Méthodes supplémentaires pour la gestion des documents

    // Récupère une liste de documents par statut
    List<DocumentsDto> getDocumentsByStatut(String statut);

    // Récupère une liste de documents par nom de fichier
    List<DocumentsDto> getDocumentsByNomFichier(String nomFichier);

    // Récupère une liste de documents par date de début
    List<DocumentsDto> getDocumentsByDateDebut(String dateDebut);

    // Récupère une liste de documents par date de fin
    List<DocumentsDto> getDocumentsByDateFin(String dateFin);

    // Récupère une liste de documents par voyage d'étude
    List<DocumentsDto> getDocumentsByVoyageEtudeId(Long voyageEtudeId);

    // Récupère une liste de documents par subvention
    List<DocumentsDto> getDocumentsBySubventionId(Long subventionId);

    // Récupère une liste de documents par candidature
    List<DocumentsDto> getDocumentsByCandidatureId(Long candidatureId);
}
