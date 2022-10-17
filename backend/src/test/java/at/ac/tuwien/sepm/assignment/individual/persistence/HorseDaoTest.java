package at.ac.tuwien.sepm.assignment.individual.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseDaoTest {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  HorseDao horseDao;

  @Test
  public void getAllReturnsAllStoredHorses() {
    LOG.trace("getAllReturnsAllStoredHorses");
    List<Horse> horses = horseDao.getAll();
    assertThat(horses.size()).isGreaterThanOrEqualTo(10);
    assertThat(horses)
        .extracting(Horse::getId, Horse::getName)
        .contains(tuple(-1L, "Wendy"));
  }

  @Test
  public void getNonExistentThrowsNotFound() {
    LOG.trace("getNonExistentThrowsNotFound");
    assertThrows(NotFoundException.class, () -> horseDao.getById(-9999L));
  }

  @Test
  public void afterDeletingHorseItShouldNotBeFindableInDB() throws NotFoundException, ConflictException {
    LOG.trace("afterDeletingHorseItShouldNotBeFindableInDB");
    Horse h = horseDao.create(new HorseDetailDto(null, "TestHorseName", null, LocalDate.now(), Sex.FEMALE, null, null, null));
    horseDao.delete(h.getId());
    assertThrows(NotFoundException.class, () -> horseDao.getById(h.getId()));
  }

  @Test
  public void creatingHorseFromDtoShouldReturnSameValues() throws NotFoundException, ConflictException {
    LOG.trace("creatingHorseFromDtoShouldReturnSameValues");
    HorseDetailDto toBeCreated = new HorseDetailDto(
        null,
        "Wendy 2.0",
        "Better, faster, stronger.",
        LocalDate.of(2022, 1, 1),
        Sex.MALE,
        new OwnerDto(-1L, "Owner", "LastName", null),
        null,
        null

    );
    Horse created = horseDao.create(toBeCreated);
    Horse shouldHaveBeenCreated = new Horse()
        .setId(created.getId())
        .setName("Wendy 2.0")
        .setDescription("Better, faster, stronger.")
        .setDateOfBirth(LocalDate.of(2022, 1, 1))
        .setSex(Sex.MALE).setOwnerId(-1L)
        .setMotherId(null)
        .setFatherId(null);

    assertEquals(created, shouldHaveBeenCreated);

    // cleanup:
    horseDao.delete(created.getId());
  }
}
