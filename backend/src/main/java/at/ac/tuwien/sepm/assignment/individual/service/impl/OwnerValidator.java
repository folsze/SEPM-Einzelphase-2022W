package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class OwnerValidator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void validateForCreate(OwnerCreateDto ownerCreateDto) throws ValidationException {
        LOG.trace("validateForUpdate({})", ownerCreateDto);
        List<String> validationErrors = new ArrayList<>();
        validateFirstName(ownerCreateDto.firstName(), validationErrors);
        validateLastName(ownerCreateDto.lastName(), validationErrors);
        validateEmail(ownerCreateDto.email(), validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of owner to be created failed", validationErrors);
        }
    }

    public void validateForSearch(OwnerSearchDto ownerSearchDto) throws ValidationException {
        LOG.trace("validateForSearch({})", ownerSearchDto);
        List<String> validationErrors = new ArrayList<>();

        validateOwnerFullNameSubstring(ownerSearchDto.name(),validationErrors);

        if (ownerSearchDto.maxAmount() != null && ownerSearchDto.maxAmount() <= 0) {
            validationErrors.add("Max result count must be > 0");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of owner to be created failed", validationErrors);
        }
    }

    private void validateEmail(String email, List<String> validationErrors) {
        String regEx = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        if (email != null) {
            if (!email.matches(regEx)){
                validationErrors.add("Invalid e-mail for new owner");
            }
        }
    }

    private void validateFirstName(String firstName, List<String> validationErrors) {
        if (firstName != null) {
            if (firstName.isBlank()) {
                validationErrors.add("First name is given but blank");
            }

            if (firstName.length() > 255) {
                validationErrors.add("First name too long: longer than 255 characters");
            }

            if (firstName.length() != firstName.trim().length()) {
                validationErrors.add("First name must not start/end with whitespaces");
            }
        } else {
            validationErrors.add("First name must not be null");
        }
    }

    private void validateLastName(String lastName, List<String> validationErrors) {
        if (lastName != null) {
            if (lastName.isBlank()) {
                validationErrors.add("Last name is given but blank");
            }

            if (lastName.length() > 255) {
                validationErrors.add("Last name too long: longer than 255 characters");
            }

            if (lastName.length() != lastName.trim().length()) {
                validationErrors.add("Last name must not start/end with whitespaces");
            }
        } else {
            validationErrors.add("Last name must not be null");
        }
    }

    private void validateOwnerFullNameSubstring(String ownerFullNameSubstring, List<String> validationErrors) {
        if (ownerFullNameSubstring != null) {
            if (ownerFullNameSubstring.isBlank()) {
                validationErrors.add("Full name substring is given but blank");
            }
            if (ownerFullNameSubstring.length() > 255+1+255) {
                validationErrors.add("Full name substring too long: longer than 255+1+255 characters (1=whitespace)");
            }

            if (ownerFullNameSubstring.length() != ownerFullNameSubstring.trim().length()) {
                validationErrors.add("Full name substring must not start/end with whitespaces");
            }
        }
    }

}
