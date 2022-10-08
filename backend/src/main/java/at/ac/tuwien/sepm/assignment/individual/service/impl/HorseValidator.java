package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForCreate(HorseDetailDto horse) throws ValidationException {
    List<String> validationErrors = new ArrayList<>();
    validateGenericHorseData(horse, validationErrors);
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

    validateGenericHorseData(horse, validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  private void validateGenericHorseData(HorseDetailDto horse, List<String> validationErrors) {
    if (horse.name() != null) {
      if (horse.name().isBlank()) {
        validationErrors.add("Horse name is given but blank");
      }
      if (horse.name().length() > 255) {
        validationErrors.add("Horse name too long: longer than 255 characters");
      }

      if (horse.name().length() != horse.name().trim().length()) {
        validationErrors.add("Horse name must not start/end with whitespaces");
      }

      if (horse.name().trim().contains(" ")) {  // trim because space at start/end was already checked
        validationErrors.add("Horse name must not contain whitespaces");
      }
    } else {
      validationErrors.add("Horse name must be specified.");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }

      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }

      if (horse.description().length() != horse.description().trim().length()) {
        validationErrors.add("Horse description must not start/end with whitespaces");
      }
    }

    if (horse.dateOfBirth() != null) {
      if (horse.dateOfBirth().isAfter(LocalDate.now())){
        validationErrors.add("Date of birth must not be in the future.");
      }
    } else {
      validationErrors.add("Date of birth must be specified.");
    }

    if (horse.sex() == null) {
      validationErrors.add("Sex must be specified.");
    }
  }
}
