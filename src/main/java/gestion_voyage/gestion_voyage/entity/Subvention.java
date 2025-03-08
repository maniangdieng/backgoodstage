package gestion_voyage.gestion_voyage.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "subvention")
public class Subvention implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type_activite", nullable = false)
    String typeActivite;

    @Column(name = "montant", nullable = false)
    Double montant;

    @Column(name = "statut", nullable = false)
    String statut;

    // Relation avec Documents
    @OneToMany(mappedBy = "subvention", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Documents> documents;

    // Relation avec Personnel
    @ManyToOne
    @JoinColumn(name = "personnel_id", referencedColumnName = "id", nullable = false)
    Personnel personnel;
}
