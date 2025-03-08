
package gestion_voyage.gestion_voyage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organisation")
public class Organisation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organisation")
    Long idOrganisation; // Identifiant unique

    @Column(name = "nom_organisation", nullable = false, length = 30)
    String nomOrganisation;

    @Column(name = "email", nullable = false, length = 50)
    String email;

    @Column(name = "telephone", nullable = false, length = 15)
    String telephone;

    // Relation avec Responsabilite
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Responsabilite> responsabilites = new HashSet<>();

}