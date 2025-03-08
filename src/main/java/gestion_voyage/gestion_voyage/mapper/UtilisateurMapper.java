package gestion_voyage.gestion_voyage.mapper;
import gestion_voyage.gestion_voyage.dto.UtilisateurDto;
import gestion_voyage.gestion_voyage.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component

public class UtilisateurMapper {

    // Conversion de l'entité vers le DTO
    public UtilisateurDto toDto(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setLogin(utilisateur.getLogin());
        dto.setMotDePasse(utilisateur.getMotDePasse());
        dto.setRole(utilisateur.getRole());
        dto.setTelephone(utilisateur.getTelephone());

        return dto;
    }

    // Conversion du DTO vers l'entité
    public Utilisateur toEntity(UtilisateurDto dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(dto.getId());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setLogin(dto.getLogin());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setRole(dto.getRole());
        utilisateur.setTelephone(dto.getTelephone());

        return utilisateur;
    }
}