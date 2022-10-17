package at.ac.tuwien.sepm.assignment.individual.entity;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a horse in the persistent data store.
 */
public class Horse {
  private Long id;
  private String name;
  private String description;
  private LocalDate dateOfBirth;
  private Sex sex;
  private Long ownerId;
  private Long motherId;
  private Long fatherId;

  public Long getId() {
    return id;
  }

  public Horse setId(Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Horse setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Horse setDescription(String description) {
    this.description = description;
    return this;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public Horse setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  public Sex getSex() {
    return sex;
  }

  public Horse setSex(Sex sex) {
    this.sex = sex;
    return this;
  }


  public Long getOwnerId() {
    return ownerId;
  }

  public Horse setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Horse horse = (Horse) o;
    return id.equals(horse.id) && name.equals(horse.name) && Objects.equals(description, horse.description) && dateOfBirth.equals(horse.dateOfBirth)
        && sex == horse.sex && Objects.equals(ownerId, horse.ownerId) && Objects.equals(motherId, horse.motherId)
        && Objects.equals(fatherId, horse.fatherId);
  }

  public Long getMotherId() {
    return motherId;
  }

  public Horse setMotherId(Long motherId) {
    this.motherId = motherId;
    return this;
  }

  public Long getFatherId() {
    return fatherId;
  }

  public Horse setFatherId(Long fatherId) {
    this.fatherId = fatherId;
    return this;
  }

  @Override
  public String toString() {
    return "Horse{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + ", dateOfBirth=" + dateOfBirth
        + ", sex=" + sex
        + ", ownerId=" + ownerId
        + ", motherId=" + motherId
        + ", fatherId=" + fatherId
        + '}';
  }
}
