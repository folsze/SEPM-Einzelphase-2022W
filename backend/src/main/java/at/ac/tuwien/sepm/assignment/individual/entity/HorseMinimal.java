package at.ac.tuwien.sepm.assignment.individual.entity;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

public class HorseMinimal {
  private Long id;
  private String name;
  private LocalDate dateOfBirth;
  private Sex sex;

  @Override
  public String toString() {
    return "HorseMinimal{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", dateOfBirth=" + dateOfBirth
        + ", sex=" + sex
        + '}';
  }

  public Long getId() {
    return id;
  }

  public HorseMinimal setId(Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public HorseMinimal setName(String name) {
    this.name = name;
    return this;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public HorseMinimal setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  public Sex getSex() {
    return sex;
  }

  public HorseMinimal setSex(Sex sex) {
    this.sex = sex;
    return this;
  }
}
