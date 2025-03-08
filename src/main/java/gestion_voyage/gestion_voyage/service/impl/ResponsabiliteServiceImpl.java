package gestion_voyage.gestion_voyage.service.impl;
import gestion_voyage.gestion_voyage.entity.Responsabilite;
import gestion_voyage.gestion_voyage.repository.ResponsabiliteRepository;
import gestion_voyage.gestion_voyage.service.ResponsabiliteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ResponsabiliteServiceImpl implements ResponsabiliteService  {


    @Autowired
    private ResponsabiliteRepository responsabiliteRepository;

    @Override
    public List<Responsabilite> getAllResponsabilites() {
        return responsabiliteRepository.findAll();
    }

    @Override
    public Optional<Responsabilite> getResponsabiliteById(Long id) {
        return responsabiliteRepository.findById(id);
    }

    @Override
    public Optional<Responsabilite> getResponsabiliteByLibelle(String libelle) {
        return responsabiliteRepository.findByLibelle(libelle);
    }

    @Override
    public List<Responsabilite> getResponsabilitesActives() {
        return responsabiliteRepository.findByDateFinIsNull();
    }

    @Override
    public List<Responsabilite> getResponsabilitesByDateDebutBetween(LocalDate startDate, LocalDate endDate) {
        return responsabiliteRepository.findByDateDebutBetween(startDate, endDate);
    }

    @Override
    public Responsabilite saveResponsabilite(Responsabilite responsabilite) {
        // Optionnel : ajouter une validation avant la sauvegarde
        return responsabiliteRepository.save(responsabilite);
    }

    @Override
    public void deleteResponsabiliteById(Long id) {
        if (responsabiliteRepository.existsById(id)) {
            responsabiliteRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Responsabilité avec ID " + id + " n'existe pas.");
        }
    }

    @Override
    public void deleteResponsabiliteByLibelle(String libelle) {
        // Optionnel : vérifier si la responsabilité existe avant la suppression
        Optional<Responsabilite> responsabiliteOpt = responsabiliteRepository.findByLibelle(libelle);
        if (responsabiliteOpt.isPresent()) {
            responsabiliteRepository.deleteByLibelle(libelle);
        } else {
            throw new IllegalArgumentException("Responsabilité avec libellé '" + libelle + "' n'existe pas.");
        }
    }
}