package at.ac.tuwien.sepm.assignment.individual.mapper;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseMinimalDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.entity.HorseMinimal;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public HorseMapper() {

  }

  public HorseMinimalDto minimalEntityToMinimalDto(HorseMinimal horse) {
    LOG.trace("minimalEntityToMinimalDto {}", horse);
    if (horse == null) {
      return null;
    }
    return new HorseMinimalDto(horse.getId(), horse.getName(), horse.getDateOfBirth(), horse.getSex());
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse  the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseListDto entityToListDto(Horse horse, Map<Long, OwnerDto> owners) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseListDto(
        horse.getId(),
        horse.getName(),
        horse.getDescription(),
        horse.getDateOfBirth(),
        horse.getSex(),
        getOwnerDto(horse.getOwnerId(), owners)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse  the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseDetailDto entityToDetailDto(
      Horse horse,
      Map<Long, OwnerDto> owners,
      Map<Long, HorseMinimalDto> mothers,
      Map<Long, HorseMinimalDto> fathers
  ) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseDetailDto(
        horse.getId(),
        horse.getName(),
        horse.getDescription(),
        horse.getDateOfBirth(),
        horse.getSex(),
        getOwnerDto(horse.getOwnerId(), owners),
        getHorseMinimalDto(horse.getMotherId(), mothers),
        getHorseMinimalDto(horse.getFatherId(), fathers)
    );
  }

  private OwnerDto getOwnerDto(Long ownerId, Map<Long, OwnerDto> owners) {
    LOG.trace("getOwnerDto {}", ownerId);
    OwnerDto owner = null;
    if (ownerId != null) {
      if (!owners.containsKey(ownerId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(ownerId)); // where would this be needed?
      }
      owner = owners.get(ownerId);
    }
    return owner;
  }

  private HorseMinimalDto getHorseMinimalDto(Long horseId, Map<Long, HorseMinimalDto> horses) {
    LOG.trace("getHorseMinimalDto {}", horseId);
    HorseMinimalDto horse = null;
    if (horseId != null) {
      if (!horses.containsKey(horseId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horseId)); // where would this be needed?
      }
      horse = horses.get(horseId);
    }
    return horse;
  }

}
