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
@Table(name = "voyage_etude", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session"})})
public class VoyageEtude implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "date_creation")
    LocalDate dateCreation;

    @Column(name = "annee")
    Integer annee;

    @Column(name = "observation")
    String observation;

    @Column(name = "date_voyage")
    LocalDate dateVoyage;

    @Column(name = "date_retour")
    LocalDate dateRetour;

    @Column(name = "statut")
    String statut;

    @Column(name = "session")
    String session;

    // Relation avec Documents
    @OneToMany(mappedBy = "voyageEtude", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Documents> documents;

    // Relation 1:1 avec Candidature
    @OneToOne(mappedBy = "voyageEtude", cascade = CascadeType.ALL, orphanRemoval = true)
    Candidature candidature;
}
