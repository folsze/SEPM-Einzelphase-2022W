package at.ac.tuwien.sepm.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseMinimalDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  HorseService horseService;

  @Test
  public void getAllReturnsAllStoredHorses() throws ValidationException, ConflictException {
    LOG.trace("getAllReturnsAllStoredHorses");
    HorseSearchDto h = new HorseSearchDto(null, null, null, null, null, null, null);
    List<HorseListDto> horses = horseService.search(h)
        .toList();
    assertThat(horses.size()).isGreaterThanOrEqualTo(10);
    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.FEMALE));
  }

  @Test
  public void shouldNotCreateHorseWithSameSexParents() throws ValidationException, NotFoundException, ConflictException {
    LOG.trace("shouldNotCreateHorseWithSameSexParents");
    HorseDetailDto mother = new HorseDetailDto(null, "mom", null, LocalDate.of(2022, 1, 1), Sex.FEMALE, null, null, null);
    HorseDetailDto mother2 = new HorseDetailDto(null, "mom2", null, LocalDate.of(2022, 1, 1), Sex.FEMALE, null, null, null);
    HorseDetailDto h1 = horseService.create(mother);
    HorseDetailDto h2 = horseService.create(mother2);
    HorseMinimalDto min1 = new HorseMinimalDto(h1.id(), h1.name(), h1.dateOfBirth(), h1.sex());
    HorseMinimalDto min2 = new HorseMinimalDto(h2.id(), h2.name(), h2.dateOfBirth(), h2.sex());

    HorseDetailDto toBeCreated = new HorseDetailDto(null, "child", null, LocalDate.of(2022, 1, 2), Sex.FEMALE, null, min1, min2);
    assertThrows(ValidationException.class, () -> horseService.create(toBeCreated));

    // cleanup
    horseService.delete(h1.id());
    horseService.delete(h2.id());
  }

  @Test
  public void shouldNotCreateHorseWithoutName() {
    LOG.trace("shouldNotCreateHorseWithoutName");
    HorseDetailDto horse = new HorseDetailDto(null, null, null, LocalDate.of(2022, 1, 1), Sex.FEMALE, null, null, null);
    assertThrows(ValidationException.class, () -> horseService.create(horse));
  }

  @Test
  public void shouldUpdateWhenParentsDifferentSex() throws ValidationException, NotFoundException, ConflictException {
    LOG.trace("shouldUpdateWhenParentsDifferentSex");
    HorseDetailDto h1 = new HorseDetailDto(null, "mom", null, LocalDate.of(2022, 1, 1),
        Sex.FEMALE, null, null, null);
    HorseDetailDto h2 = new HorseDetailDto(null, "dad", null, LocalDate.of(2022, 1, 1),
        Sex.MALE, null, null, null);
    HorseDetailDto c1 = horseService.create(h1);
    HorseDetailDto c2 = horseService.create(h2);

    HorseMinimalDto min1 = new HorseMinimalDto(c1.id(), h1.name(), h1.dateOfBirth(), h1.sex());
    HorseMinimalDto min2 = new HorseMinimalDto(c2.id(), h2.name(), h2.dateOfBirth(), h2.sex());


    HorseDetailDto updateWithThis = new HorseDetailDto(null,
        "WendyWithParents",
        null,
        LocalDate.of(2022, 1, 2),
        Sex.FEMALE,
        null,
        min1,
        min2);
    horseService.update(-1L, updateWithThis);

    HorseDetailDto updatedWendy = horseService.getById(-1L);
    HorseDetailDto wendyShouldHaveBeenUpdatedLikeThis = new HorseDetailDto(
        -1L,
        "WendyWithParents",
        null,
        LocalDate.of(2022, 1, 2),
        Sex.FEMALE,
        null,
        min1,
        min2
    );
    assertEquals(updatedWendy, wendyShouldHaveBeenUpdatedLikeThis);
  }
}
