package gestion_voyage.gestion_voyage.controller;

import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.service.DocumentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")

public class DocumentsController {

    @Autowired
    private DocumentsService documentsService;

    // POST: Créer un document
    @PostMapping
    public ResponseEntity<DocumentsDto> createDocument(@RequestBody DocumentsDto documentsDto) {
        DocumentsDto savedDocument = documentsService.createDocument(documentsDto);
        return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
    }

    // GET: Récupérer un document par ID
    @GetMapping("/{id}")
    public ResponseEntity<DocumentsDto> getDocumentById(@PathVariable Long id) {
        DocumentsDto documentDto = documentsService.getDocumentById(id);
        return new ResponseEntity<>(documentDto, HttpStatus.OK);
    }

    // GET: Récupérer tous les documents
    @GetMapping
    public ResponseEntity<List<DocumentsDto>> getAllDocuments() {
        List<DocumentsDto> documents = documentsService.getAllDocuments();
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    // PUT: Mettre à jour un document
    @PutMapping("/{id}")
    public ResponseEntity<DocumentsDto> updateDocument(@PathVariable Long id, @RequestBody DocumentsDto documentsDto) {
        DocumentsDto updatedDocument = documentsService.updateDocument(id, documentsDto);
        return new ResponseEntity<>(updatedDocument, HttpStatus.OK);
    }

    // DELETE: Supprimer un document par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentsService.deleteDocument(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // GET: Récupérer des documents par statut
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<DocumentsDto>> getDocumentsByStatut(@PathVariable String statut) {
        List<DocumentsDto> documents = documentsService.getDocumentsByStatut(statut);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    // GET: Récupérer des documents par nom de fichier
    @GetMapping("/nom-fichier/{nomFichier}")
    public ResponseEntity<List<DocumentsDto>> getDocumentsByNomFichier(@PathVariable String nomFichier) {
        List<DocumentsDto> documents = documentsService.getDocumentsByNomFichier(nomFichier);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    // GET: Récupérer des documents par date de début
    @GetMapping("/date-debut/{dateDebut}")
    public ResponseEntity<List<DocumentsDto>> getDocumentsByDateDebut(@PathVariable String dateDebut) {
        List<DocumentsDto> documents = documentsService.getDocumentsByDateDebut(dateDebut);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    // GET: Récupérer des documents par date de fin
    @GetMapping("/date-fin/{dateFin}")
    public ResponseEntity<List<DocumentsDto>> getDocumentsByDateFin(@PathVariable String dateFin) {
        List<DocumentsDto> documents = documentsService.getDocumentsByDateFin(dateFin);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    // GET: Récupérer des documents par ID de voyage d'étude
    @GetMapping("/voyage-etude/{voyageEtudeId}")
    public ResponseEntity<List<DocumentsDto>> getDocumentsByVoyageEtudeId(@PathVariable Long voyageEtudeId) {
        List<DocumentsDto> documents = documentsService.getDocumentsByVoyageEtudeId(voyageEtudeId);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    // GET: Récupérer des documents par ID de subvention
    @GetMapping("/subvention/{subventionId}")
    public ResponseEntity<List<DocumentsDto>> getDocumentsBySubventionId(@PathVariable Long subventionId) {
        List<DocumentsDto> documents = documentsService.getDocumentsBySubventionId(subventionId);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    // GET: Récupérer des documents par ID de candidature
    @GetMapping("/candidature/{candidatureId}")
    public ResponseEntity<List<DocumentsDto>> getDocumentsByCandidatureId(@PathVariable Long candidatureId) {
        List<DocumentsDto> documents = documentsService.getDocumentsByCandidatureId(candidatureId);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }
}

