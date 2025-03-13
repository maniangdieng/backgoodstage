package gestion_voyage.gestion_voyage.service.impl;
import gestion_voyage.gestion_voyage.entity.Cohorte;
import gestion_voyage.gestion_voyage.repository.CohorteRepository;
import gestion_voyage.gestion_voyage.service.CohorteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CohorteServiceImpl implements CohorteService {


    private final CohorteRepository cohorteRepository;

    @Autowired
    public CohorteServiceImpl(CohorteRepository cohorteRepository) {
        this.cohorteRepository = cohorteRepository;
    }

    @Override
    public List<Cohorte> getAllCohortes() {
        return cohorteRepository.findAll();
    }

    @Override
    public Optional<Cohorte> getCohorteById(Long id) {
        return cohorteRepository.findById(id);
    }

    @Override
    public Optional<Cohorte> getCohorteByAnnee(Integer annee) {
        return cohorteRepository.findByAnnee(annee);  // Correction ici pour utiliser 'findByAnnee'
    }

    @Override
    public List<Cohorte> getCohortesByDateClotureDefBefore(LocalDate date) {
        return cohorteRepository.findByDateClotureDefBefore(date);  // Correction pour utiliser 'dateClotureDef'
    }

    @Override
    public List<Cohorte> getCohortesByDateSemiClotureAfter(LocalDate date) {
        return cohorteRepository.findByDateSemiClotureAfter(date);
    }

    @Override
    public Cohorte saveCohorte(Cohorte cohorte) {
      // Vérifier l'unicité de l'année
      Optional<Cohorte> existingCohorte = cohorteRepository.findByAnnee(cohorte.getAnnee());
      if (existingCohorte.isPresent() && !existingCohorte.get().getId().equals(cohorte.getId())) {
        throw new IllegalArgumentException("Une cohorte existe déjà pour cette année.");
      }

      // Vérifier l'ordre des dates
      if (cohorte.getDateOuverture().isAfter(cohorte.getDateSemiCloture())) {
        throw new IllegalArgumentException("La date d'ouverture doit être antérieure à la date de semi-clôture.");
      }
      if (cohorte.getDateSemiCloture().isAfter(cohorte.getDateClotureDef())) {
        throw new IllegalArgumentException("La date de semi-clôture doit être antérieure à la date de clôture définitive.");
      }

      return cohorteRepository.save(cohorte);
    }
    @Override
    public void deleteCohorteById(Long id) {
        cohorteRepository.deleteById(id);
    }

    @Override
    public void deleteCohorteByAnnee(Integer annee) {
        cohorteRepository.deleteByAnnee(annee);  // Correction ici pour utiliser 'deleteByAnnee'
    }
    @Override
  public boolean existsByAnnee(int annee) {
    return cohorteRepository.existsByAnnee(annee);
  }
}
