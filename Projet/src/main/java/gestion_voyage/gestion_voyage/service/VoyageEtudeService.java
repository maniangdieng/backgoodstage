package gestion_voyage.gestion_voyage.service;
import gestion_voyage.gestion_voyage.dto.VoyageEtudeDto;
import gestion_voyage.gestion_voyage.entity.VoyageEtude;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface VoyageEtudeService {


    // Méthode pour créer un nouveau voyage d'études
    VoyageEtudeDto create(VoyageEtudeDto voyageEtudeDto);

    // Méthode pour récupérer la liste de tous les voyages d'études
    List<VoyageEtudeDto> getAllVoyagesEtudes();

    // Méthode pour obtenir un voyage d'études par son identifiant (ID)
    Optional<VoyageEtudeDto> getVoyageEtudeById(Long id);

    // Méthode pour mettre à jour un voyage d'études en fonction de son ID
    Optional<VoyageEtudeDto> updateVoyageEtude(Long id, VoyageEtudeDto voyageEtudeDto);

    // Méthode pour supprimer un voyage d'études par son ID
    boolean deleteVoyageEtude(Long id);

    // Méthode pour récupérer des voyages d'études avec pagination selon le statut
    Page<VoyageEtudeDto> getVoyagesEtudesByStatut(String statut, Pageable pageable);

    // Méthode pour récupérer des voyages d'études avec pagination selon l'année
    Page<VoyageEtudeDto> getVoyagesEtudesByAnnee(Integer annee, Pageable pageable);
}