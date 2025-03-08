package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.PersonnelDto;
import gestion_voyage.gestion_voyage.entity.Personnel;
import gestion_voyage.gestion_voyage.entity.Candidature;
import gestion_voyage.gestion_voyage.entity.Responsabilite;
import gestion_voyage.gestion_voyage.entity.Subvention;
import gestion_voyage.gestion_voyage.mapper.PersonnelMapper;
import gestion_voyage.gestion_voyage.repository.CandidatureRepository;
import gestion_voyage.gestion_voyage.repository.PersonnelRepository;
import gestion_voyage.gestion_voyage.repository.ResponsabiliteRepository;
import gestion_voyage.gestion_voyage.repository.SubventionRepository;
import gestion_voyage.gestion_voyage.service.PersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PersonnelServiceImpl implements PersonnelService {

    @Autowired
    private PersonnelRepository personnelRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private ResponsabiliteRepository responsabiliteRepository;

    @Autowired
    private SubventionRepository subventionRepository;

    @Autowired
    private PersonnelMapper personnelMapper;

    @Override
    public PersonnelDto createPersonnel(PersonnelDto personnelDto) {
        // Récupérer les entités liées
        Set<Candidature> candidatures = new HashSet<>();
        if (personnelDto.getCandidatureIds() != null) {
            candidatures = new HashSet<>(candidatureRepository.findAllById(personnelDto.getCandidatureIds()));
        }

        Set<Responsabilite> responsabilites = new HashSet<>();
        if (personnelDto.getResponsabiliteIds() != null) {
            responsabilites = new HashSet<>(responsabiliteRepository.findAllById(personnelDto.getResponsabiliteIds()));
        }

        // Récupérer la subvention associée (si présente)
        Subvention subvention = null;
        if (personnelDto.getSubventionId() != null) {
            subvention = subventionRepository.findById(personnelDto.getSubventionId()).orElse(null);
        }

        // Mapper le DTO vers l'entité
        Personnel personnel = personnelMapper.toEntity(personnelDto, subvention, candidatures, responsabilites);

        // Sauvegarder l'entité Personnel dans la base de données
        personnel = personnelRepository.save(personnel);

        // Mapper l'entité Personnel sauvegardée vers le DTO
        return personnelMapper.toDto(personnel);
    }

    @Override
    public PersonnelDto updatePersonnel(Long id, PersonnelDto personnelDto) {
        // Vérifier si le personnel existe
        Personnel existingPersonnel = personnelRepository.findById(id).orElse(null);
        if (existingPersonnel == null) {
            return null;  // Retourner null si le personnel n'existe pas
        }

        // Récupérer les entités liées
        Set<Candidature> candidatures = new HashSet<>();
        if (personnelDto.getCandidatureIds() != null) {
            candidatures = new HashSet<>(candidatureRepository.findAllById(personnelDto.getCandidatureIds()));
        }

        Set<Responsabilite> responsabilites = new HashSet<>();
        if (personnelDto.getResponsabiliteIds() != null) {
            responsabilites = new HashSet<>(responsabiliteRepository.findAllById(personnelDto.getResponsabiliteIds()));
        }

        // Récupérer la subvention associée (si présente)
        Subvention subvention = null;
        if (personnelDto.getSubventionId() != null) {
            subvention = subventionRepository.findById(personnelDto.getSubventionId()).orElse(null);
        }

        // Mapper les données mises à jour dans l'entité Personnel
        existingPersonnel = personnelMapper.toEntity(personnelDto, subvention, candidatures, responsabilites);

        // Sauvegarder l'entité Personnel mise à jour
        existingPersonnel.setId(id); // Assurer que l'ID reste inchangé
        existingPersonnel = personnelRepository.save(existingPersonnel);

        // Mapper l'entité Personnel mise à jour vers le DTO
        return personnelMapper.toDto(existingPersonnel);
    }

    @Override
    public PersonnelDto getPersonnelById(Long id) {
        Personnel personnel = personnelRepository.findById(id).orElse(null);
        if (personnel == null) {
            return null;  // Retourner null si le personnel n'existe pas
        }
        return personnelMapper.toDto(personnel);
    }

    @Override
    public void deletePersonnel(Long id) {
        personnelRepository.deleteById(id);
    }

    @Override
    public PersonnelDto getPersonnelByMatricule(String matricule) {
        // Récupérer le Personnel via le matricule (méthode retournant un Optional)
        Optional<Personnel> optionalPersonnel = personnelRepository.findByMatricule(matricule);

        // Vérifier si un personnel a été trouvé
        if (optionalPersonnel.isPresent()) {
            // Mapper et retourner le PersonnelDto si trouvé
            return personnelMapper.toDto(optionalPersonnel.get());
        } else {
            // Gérer le cas où aucun personnel n'est trouvé
            // Option 1 : Retourner null (ou tu peux lancer une exception ici)
            return null;

            // Option 2 : Lancer une exception personnalisée
            // throw new PersonnelNotFoundException("Personnel with matricule " + matricule + " not found");
        }
    }

    @Override
    public PersonnelDto getPersonnelByEmail(String email) {
        // Récupérer le Personnel via l'email (méthode retournant un Optional)
        Optional<Personnel> optionalPersonnel = personnelRepository.findByEmail(email);

        // Vérifier si un personnel a été trouvé
        if (optionalPersonnel.isPresent()) {
            // Mapper et retourner le PersonnelDto si trouvé
            return personnelMapper.toDto(optionalPersonnel.get());
        } else {
            // Gérer le cas où aucun personnel n'est trouvé

            return null;

            // throw new PersonnelNotFoundException("Personnel with email " + email + " not found");
        }
    }

    @Override
    public List<PersonnelDto> getAllPersonnels() {
        // Récupérer tous les personnels
        List<Personnel> personnels = personnelRepository.findAll();
        return personnelMapper.toDto(personnels); // Mapper et retourner la liste
    }

    @Override
    public List<PersonnelDto> getPersonnelsByType(String type) {
        // Récupérer tous les personnels ayant un type spécifique
        List<Personnel> personnels = personnelRepository.findByType(type);
        return personnelMapper.toDto(personnels); // Mapper et retourner la liste
    }

    @Override
    public List<PersonnelDto> getPersonnelsByNom(String nom) {
        // Récupérer tous les personnels ayant un nom spécifique
        List<Personnel> personnels = personnelRepository.findByNom(nom);
        return personnelMapper.toDto(personnels); // Mapper et retourner la liste
    }

    @Override
    public List<PersonnelDto> getPersonnelsByPrenom(String prenom) {
        // Récupérer tous les personnels ayant un prénom spécifique
        List<Personnel> personnels = personnelRepository.findByPrenom(prenom);
        return personnelMapper.toDto(personnels); // Mapper et retourner la liste
    }

    @Override
    public List<PersonnelDto> getPersonnelsByTelephone(String telephone) {
        // Récupérer tous les personnels ayant un numéro de téléphone spécifique
        List<Personnel> personnels = personnelRepository.findByTelephone(telephone);
        return personnelMapper.toDto(personnels); // Mapper et retourner la liste
    }
}
