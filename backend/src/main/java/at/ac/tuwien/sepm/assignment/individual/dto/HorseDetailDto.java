package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import java.time.LocalDate;

public record HorseDetailDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    OwnerDto owner,
    HorseMinimalDto mother,
    HorseMinimalDto father
) {

  public Long ownerId() {
    return owner == null
        ? null
        : owner.id();
  }

  public Long motherId() {
    return mother == null
            ? null
            : mother.id();
  }

  public Long fatherId() {
    return father == null
            ? null
            : father.id();
  }

}
