package gestion_voyage.gestion_voyage.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)

@Table(name = "cohorte")

public class Cohorte  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "annee", nullable = false)
    Integer annee;

    @Column(name = "date_ouverture", nullable = false)
    LocalDate dateOuverture;

    @Column(name = "date_semi_cloture", nullable = false)
    LocalDate dateSemiCloture;

    @Column(name = "date_cloture_definitive", nullable = false)
    LocalDate dateClotureDef;

    // Relation one-to-many avec Candidature
    @OneToMany(mappedBy = "cohorte")
    Set<Candidature> candidatures = new HashSet<>(); // Initialisation

    // Relation many-to-many avec Personnel
    @ManyToMany
    @JoinTable(
            name = "cohorte_personnel",
            joinColumns = @JoinColumn(name = "cohorte_id"),
            inverseJoinColumns = @JoinColumn(name = "personnel_id")
    )
    Set<Personnel> personnels = new HashSet<>(); // Initialisation

}