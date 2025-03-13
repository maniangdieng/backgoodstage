package gestion_voyage.gestion_voyage.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "candidature")
public class Candidature implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "date_depot", nullable = false)
    LocalDate dateDepot;

    @Column(name = "statut", nullable = false)
    String statut;

    @Column(name = "date_debut")
    LocalDate dateDebut;

    @Column(name = "date_fin")
    LocalDate dateFin;

    @Column(name = "destination", nullable = false)
    String destination;

    @Column(name = "commentaire")
    String commentaire;

    // Relations avec d'autres entités
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cohorte_id")
    Cohorte cohorte;

    @OneToOne
    @JoinColumn(name = "voyage_etude_id")
    VoyageEtude voyageEtude;

    // Relation avec Documents
    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Documents> documents;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "personnel_id")
    Personnel personnel;
}
