package gestion_voyage.gestion_voyage.dto;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CohorteDto implements Serializable {
    Long id;
    Integer annee;
    LocalDate dateOuverture;
    LocalDate dateSemiCloture;
    LocalDate dateClotureDef;
    Set<Long> candidatureIds;
    Set<Long> personnelIds;
}
