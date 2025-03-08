package gestion_voyage.gestion_voyage.service.impl;

import gestion_voyage.gestion_voyage.dto.UtilisateurDto;
import gestion_voyage.gestion_voyage.entity.Utilisateur;
import gestion_voyage.gestion_voyage.mapper.UtilisateurMapper;
import gestion_voyage.gestion_voyage.repository.UtilisateurRepository;
import gestion_voyage.gestion_voyage.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

public class UtilisateurServiceImpl implements UtilisateurService {


    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private UtilisateurMapper utilisateurMapper;

    @Override
    public UtilisateurDto createUtilisateur(UtilisateurDto utilisateurDto, String motDePasse) {
        Utilisateur utilisateur = utilisateurMapper.toEntity(utilisateurDto);
        utilisateur.setMotDePasse(motDePasse); // Ajout du mot de passe
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toDto(savedUtilisateur);
    }

    @Override
    public UtilisateurDto updateUtilisateur(Long id, UtilisateurDto utilisateurDto, String motDePasse) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(id);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur utilisateur = optionalUtilisateur.get();
            utilisateur.setNom(utilisateurDto.getNom());
            utilisateur.setPrenom(utilisateurDto.getPrenom());
            utilisateur.setEmail(utilisateurDto.getEmail());
            utilisateur.setLogin(utilisateurDto.getLogin());
            utilisateur.setRole(utilisateurDto.getRole());
            utilisateur.setTelephone(utilisateurDto.getTelephone());
            utilisateur.setMotDePasse(motDePasse);
            Utilisateur updatedUtilisateur = utilisateurRepository.save(utilisateur);
            return utilisateurMapper.toDto(updatedUtilisateur);
        } else {
            // Gestion d'erreur à l'aide de votre classe de gestion d'exceptions
            throw new RuntimeException("Utilisateur non trouvé");
        }
    }

    @Override
    public UtilisateurDto getUtilisateurById(Long id) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(id);
        if (optionalUtilisateur.isPresent()) {
            return utilisateurMapper.toDto(optionalUtilisateur.get());
        } else {
            // Gestion d'erreur à l'aide de votre classe de gestion d'exceptions
            throw new RuntimeException("Utilisateur non trouvé");
        }
    }

    @Override
    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    @Override
    public List<UtilisateurDto> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        return utilisateurs.stream()
                .map(utilisateurMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurDto getUtilisateurByEmail(String email) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findByEmail(email);
        if (optionalUtilisateur.isPresent()) {
            return utilisateurMapper.toDto(optionalUtilisateur.get());
        } else {
            // Gestion d'erreur à l'aide de votre classe de gestion d'exceptions
            throw new RuntimeException("Utilisateur non trouvé avec l'email : " + email);
        }
    }

    @Override
    public UtilisateurDto getUtilisateurByLogin(String login) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findByLogin(login);
        if (optionalUtilisateur.isPresent()) {
            return utilisateurMapper.toDto(optionalUtilisateur.get());
        } else {
            // Gestion d'erreur à l'aide de votre classe de gestion d'exceptions
            throw new RuntimeException("Utilisateur non trouvé avec le login : " + login);
        }
    }
}
