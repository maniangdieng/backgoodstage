package gestion_voyage.gestion_voyage.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@DiscriminatorValue("Personnel")
public class Personnel extends Utilisateur {


    @Column(name = "matricule", nullable = false, unique = true)
    String matricule;

    @Column(name = "type", nullable = false)
    String type;

    // Relation One-to-Many avec Candidature
    @OneToMany(mappedBy = "personnel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    Set<Candidature> candidatures;

    // Relation One-to-Many avec Responsabilite (lien indirect avec Organisation)
    @OneToMany(mappedBy = "personnel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    Set<Responsabilite> responsabilites;

    // Relation Many-to-One avec Subvention
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subvention_id", nullable = true)
    Subvention subvention;

    // Constructeur par défaut
    public Personnel() {
        super();
    }



}