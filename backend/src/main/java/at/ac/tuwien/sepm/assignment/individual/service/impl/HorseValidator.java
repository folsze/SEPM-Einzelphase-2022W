package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseMinimalDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.entity.HorseMinimal;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.persistence.OwnerDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final HorseDao horseDao;
  private final OwnerDao ownerDao;

  public HorseValidator(HorseDao horseDao, OwnerDao ownerDao){
    this.horseDao = horseDao;
    this.ownerDao = ownerDao;
  }

  // ------------------------------------------------------------------- START OF VALIDATE FOR CREATE OR UPDATE SECTION -------------------------------------------------------------------
  public void validateForCreate(HorseDetailDto horse) throws ValidationException {
    List<String> validationErrors = new ArrayList<>();

    validateWhatsRequiredIfCreateOrUpdate(horse,validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  private void validateWhatsRequiredIfCreateOrUpdate(HorseDetailDto horse, List<String> validationErrors) {
    validatePrimitiveHorseAttributes(horse.name(), horse.description(), horse.dateOfBirth(), horse.sex(), validationErrors);

    if (horse.ownerId() != null) {
      validateThatOwnerExistsInDB(horse.ownerId(), validationErrors);
    }

    if (horse.mother() != null) {
      if (horse.mother().sex() != Sex.FEMALE) {
        validationErrors.add("Mother's sex must be female");
      }
      validateThatHorseYoungerThanMother(horse, horse.mother(), validationErrors);
      validateThatMotherExistsInDB(horse.motherId(), validationErrors);
    }

    if (horse.father() != null) {
      if (horse.father().sex() != Sex.MALE) {
        validationErrors.add("Father's sex must be male");
      }
      validateThatHorseYoungerThanFather(horse, horse.father(), validationErrors);
      validateThatFatherExistsInDB(horse.fatherId(), validationErrors);
    }
  }

  private void validateThatHorseYoungerThanMother(HorseDetailDto horse, HorseMinimalDto mother, List<String> validationErrors) {
    if (horse.dateOfBirth().isBefore(mother.dateOfBirth())) {
      validationErrors.add("Horse must be younger than its mother");
    }
  }

  private void validateThatHorseYoungerThanFather(HorseDetailDto horse, HorseMinimalDto father, List<String> validationErrors) {
    if (horse.dateOfBirth().isBefore(father.dateOfBirth())) {
      validationErrors.add("Horse must be younger than its father");
    }
  }

  private void validateThatMotherExistsInDB(Long motherId, List<String> validationErrors) {
    try {
      horseDao.getById(motherId);
    } catch (NotFoundException nfe) {
      validationErrors.add("Could not find provided mother in the database");
    }
  }

  private void validateThatFatherExistsInDB(Long fatherId, List<String> validationErrors) {
    try {
      horseDao.getById(fatherId);
    } catch (NotFoundException nfe) {
      validationErrors.add("Could not find provided father in the database");
    }
  }

  private void validateThatOwnerExistsInDB(Long ownerId, List<String> validationErrors) {
    try {
      ownerDao.getById(ownerId);
    } catch (NotFoundException nfe) {
      validationErrors.add("Could not find provided owner in the database");
    }
  }

  private void validatePrimitiveHorseAttributes(String name, String description, LocalDate dateOfBirth, Sex sex, List<String> validationErrors) {
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
      validationErrors.add("Horse name must be specified");
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
        validationErrors.add("Date of birth must not be in the future");
      }
    } else {
      validationErrors.add("Date of birth must be specified");
    }

    if (sex == null) {
      validationErrors.add("Sex must be specified");
    }
  }

  // ------------------------------------------------------------------- START OF VALIDATE-FOR-UPDATE-ONLY SECTION ----------------------------------------------------------------------------------
  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    
    List<String> validationErrors = new ArrayList<>();
    validateWhatsRequiredIfCreateOrUpdate(horse,validationErrors);
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse-update data failed", validationErrors);
    }
    
    List<String> conflictErrors = new ArrayList<>();
    validateWhatsRequiredOnlyIfUpdate(horse, conflictErrors);
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Conflict-Validation of horse-update data failed", conflictErrors);
    }
  }

  private void validateWhatsRequiredOnlyIfUpdate(HorseDetailDto horse, List<String> conflictErrors) {
    if (horse.id() != null) {
      Horse oldHorse;
      try {
        oldHorse = horseDao.getById(horse.id());

        if (oldHorse.getSex() != horse.sex()) {
          validateThatHorseDoesntHaveChildren(horse.id(), conflictErrors);
        }

        if (oldHorse.getDateOfBirth() != horse.dateOfBirth()) {
            validateThatHorsesChildrenAreStillYoungerThanTheirParent(horse.id(), horse.dateOfBirth(), conflictErrors);
        }
      } catch (NotFoundException ignored) {
        conflictErrors.add("Horse to be updated was not found in database. Could not validate remaining horse values");
      }
    }  // else block was already handled by basic attribute validation
  }

  private void validateThatHorsesChildrenAreStillYoungerThanTheirParent(Long parentId, LocalDate horseDateOfBirth, List<String> conflictErrors) {
    List<HorseMinimal> children = horseDao.getChildrenOf(parentId);

    if (children.size() > 0) {
      List<String> childrenOlderThanTheirParent = new ArrayList<>();
      for (HorseMinimal child : children) {
        if (child.getDateOfBirth().isBefore(horseDateOfBirth)) {
          childrenOlderThanTheirParent.add(child.getName());
        }
      }
      if (childrenOlderThanTheirParent.size() > 0) {
        conflictErrors.add("Cannot change date of birth: When moving this horse's date of birth into the future as at least one of its children would become older than the horse");
        conflictErrors.add("The following children would become older than their parent:\n" + Arrays.toString(childrenOlderThanTheirParent.toArray()));
      }
    }
  }

  private void validateThatHorseDoesntHaveChildren(Long horseId, List<String> conflictErrors) {
    List<HorseMinimal> children = horseDao.getChildrenOf(horseId);

    if (children.size() > 0) {
      List<String> childrenNames = new ArrayList<>();
      for (HorseMinimal child : children) {
        childrenNames.add(child.getName());
      }
      conflictErrors.add("Cannot change the sex of the horse since at least one child already has it as their (female/male) parent");
      conflictErrors.add("The following horse-children already have this horse as their parent:\n" + Arrays.toString(childrenNames.toArray()));
    }
  }

  // ------------------------------------------------------------------- START OF "THE REST" SECTION ----------------------------------------------------------------------------------
  public void validateForSearch(HorseSearchDto horse) throws ValidationException {
    LOG.trace("validateForSearch({})", horse);
    List<String> validationErrors = new ArrayList<>();

    validateSearchHorsePrimitiveAttributes(horse.name(), horse.description(), horse.bornBefore(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse search filter failed", validationErrors);
    }
  }

  public void validateSearchHorsePrimitiveAttributes(String name, String description, LocalDate dateOfBirth, List<String> validationErrors) {
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
