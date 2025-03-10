package gestion_voyage.gestion_voyage.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "documents")
public class Documents implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "statut", nullable = false)
    String statut;

    @Column(name = "date_debut")
    LocalDate dateDebut;

    @Column(name = "date_fin")
    LocalDate dateFin;

    @Column(name = "nom_fichier", nullable = false)
    String nomFichier;
  @Lob
  @Column(name = "contenu", nullable = false)
  byte[] contenu;

  // Association avec VoyageEtude
    @ManyToOne
    @JoinColumn(name = "voyage_etude_id", referencedColumnName = "id", nullable = false)
    VoyageEtude voyageEtude; // Changement de VoyageEtude à voyageEtude

    // Association avec Subvention
    @ManyToOne
    @JoinColumn(name = "subvention_id", referencedColumnName = "id", nullable = false)
    Subvention subvention;

    // Association avec Candidature
    @ManyToOne
    @JoinColumn(name = "candidature_id", referencedColumnName = "id", nullable = false)
    Candidature candidature;

}
