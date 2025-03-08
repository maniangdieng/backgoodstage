package gestion_voyage.gestion_voyage.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_personnel")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Gestion automatique de l'auto-incrémentation
   Long id;

    @Column(name = "nom", nullable = false, length = 50)
   String nom;

    @Column(name = "prenom", nullable = false, length = 50)
   String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    String email;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    String login;

    @Column(name = "mot_de_passe", nullable = false, unique = true, length = 30)
    String motDePasse;


    @Column(name = "role", nullable = false, length = 20)
    String role = "USER"; // Rôle par défaut


    @Column(name = "telephone", length = 15)
    String telephone;

}
