package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.DocumentsDto;
import gestion_voyage.gestion_voyage.entity.Documents;
import gestion_voyage.gestion_voyage.entity.VoyageEtude;
import gestion_voyage.gestion_voyage.entity.Subvention;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.exception.ResourceNotFoundException;
import gestion_voyage.gestion_voyage.mapper.DocumentsMapper;
import gestion_voyage.gestion_voyage.repository.DocumentsRepository;
import gestion_voyage.gestion_voyage.service.DocumentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class DocumentsServiceImpl implements DocumentsService {

@Autowired
    private final DocumentsRepository documentsRepository;
    private final DocumentsMapper documentsMapper;

    @Override
    public DocumentsDto createDocument(DocumentsDto documentsDto) {
        Documents documents = documentsMapper.toEntity(documentsDto);
        Documents savedDocument = documentsRepository.save(documents);
        return documentsMapper.toDto(savedDocument);
    }

    @Override
    public DocumentsDto getDocumentById(Long id) {
        Documents document = documentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documents", "id", id));
        return documentsMapper.toDto(document);
    }

    @Override
    public List<DocumentsDto> getAllDocuments() {
        return documentsRepository.findAll().stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentsDto updateDocument(Long id, DocumentsDto documentsDto) {
        Documents documents = documentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documents", "id", id));

        documents.setStatut(documentsDto.getStatut());
        documents.setDateDebut(documentsDto.getDateDebut());
        documents.setDateFin(documentsDto.getDateFin());
        documents.setNomFichier(documentsDto.getNomFichier());

        if (documentsDto.getVoyageEtudeId() != null) {
            VoyageEtude voyageEtude = new VoyageEtude();
            voyageEtude.setId(documentsDto.getVoyageEtudeId());
            documents.setVoyageEtude(voyageEtude);
        }

        if (documentsDto.getSubventionId() != null) {
            Subvention subvention = new Subvention();
            subvention.setId(documentsDto.getSubventionId());
            documents.setSubvention(subvention);
        }

        if (documentsDto.getCandidatureId() != null) {
            Candidature candidature = new Candidature();
            candidature.setId(documentsDto.getCandidatureId());
            documents.setCandidature(candidature);
        }

        Documents updatedDocument = documentsRepository.save(documents);
        return documentsMapper.toDto(updatedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        Documents documents = documentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documents", "id", id));
        documentsRepository.delete(documents);
    }

    @Override
    public List<DocumentsDto> getDocumentsByStatut(String statut) {
        return documentsRepository.findByStatut(statut).stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentsDto> getDocumentsByNomFichier(String nomFichier) {
        return documentsRepository.findByNomFichier(nomFichier).stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentsDto> getDocumentsByDateDebut(String dateDebut) {
        LocalDate parsedDate = LocalDate.parse(dateDebut);
        return documentsRepository.findByDateDebut(parsedDate).stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentsDto> getDocumentsByDateFin(String dateFin) {
        LocalDate parsedDate = LocalDate.parse(dateFin);
        return documentsRepository.findByDateFin(parsedDate).stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentsDto> getDocumentsByVoyageEtudeId(Long voyageEtudeId) {
        return documentsRepository.findByVoyageEtudeId(voyageEtudeId).stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentsDto> getDocumentsBySubventionId(Long subventionId) {
        return documentsRepository.findBySubventionId(subventionId).stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentsDto> getDocumentsByCandidatureId(Long candidatureId) {
        return documentsRepository.findByCandidatureId(candidatureId).stream()
                .map(documentsMapper::toDto)
                .collect(Collectors.toList());
    }
}