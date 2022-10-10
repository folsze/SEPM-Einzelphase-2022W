package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForCreate(HorseDetailDto horse) throws ValidationException {
    List<String> validationErrors = new ArrayList<>();
    validateGenericHorseData(horse.name(), horse.description(), horse.dateOfBirth(), horse.sex(), validationErrors);
    // todo: validate owner
    // todo: validate parents
      // todo: validate mother vs. father
      // todo: validate mother
      // todo: validate father
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }

    validateGenericHorseData(horse.name(), horse.description(), horse.dateOfBirth(), horse.sex(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  private void validateGenericHorseData(String name, String description, LocalDate dateOfBirth, Sex sex, List<String> validationErrors) {
    if (name != null) {
      if (name.isBlank()) {
        validationErrors.add("Horse name is given but blank");
      }
      if (name.length() > 255) {
        validationErrors.add("Horse name too long: longer than 255 characters");
      }

      if (name.length() != name.trim().length()) {
        validationErrors.add("Horse name must not start/end with whitespaces");
      }

      if (name.trim().contains(" ")) {  // trim because space at start/end was already checked
        validationErrors.add("Horse name must not contain whitespaces");
      }
    } else {
      validationErrors.add("Horse name must be specified.");
    }

    if (description != null) {
      if (description.isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }

      if (description.length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }

      if (description.length() != description.trim().length()) {
        validationErrors.add("Horse description must not start/end with whitespaces");
      }
    }

    if (dateOfBirth != null) {
      if (dateOfBirth.isAfter(LocalDate.now())){
        validationErrors.add("Date of birth must not be in the future.");
      }
    } else {
      validationErrors.add("Date of birth must be specified.");
    }

    if (sex == null) {
      validationErrors.add("Sex must be specified.");
    }
  }

  public void validateForSearch(HorseSearchDto horse) throws ValidationException {
    LOG.trace("validateForSearch({})", horse);
    List<String> validationErrors = new ArrayList<>();

    validateSearchHorseData(horse.name(), horse.description(), horse.bornBefore(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse search filter failed", validationErrors);
    }
  }

  public void validateSearchHorseData(String name, String description, LocalDate dateOfBirth, List<String> validationErrors) {
    if (name != null) {
      if (name.isBlank()) {
        validationErrors.add("Horse name is given but blank");
      }
      if (name.length() > 255) {
        validationErrors.add("Horse name too long: longer than 255 characters");
      }

      if (name.length() != name.trim().length()) {
        validationErrors.add("Horse name must not start/end with whitespaces");
      }

      if (name.trim().contains(" ")) {  // trim because space at start/end was already checked
        validationErrors.add("Horse name must not contain whitespaces");
      }
    }

    if (description != null) {
      if (description.isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }

      if (description.length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }

      if (description.length() != description.trim().length()) {
        validationErrors.add("Horse description must not start/end with whitespaces");
      }
    }

    if (dateOfBirth != null) {
      if (dateOfBirth.isAfter(LocalDate.now())){
        validationErrors.add("Date of birth must not be in the future.");
      }
    }
  }
}
