package at.ac.tuwien.sepm.assignment.individual.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @Autowired
  private HorseService horseService;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  @Test
  public void gettingAllHorses() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult.size()).isGreaterThanOrEqualTo(10);
    assertThat(horseResult).extracting(HorseListDto::id, HorseListDto::name).contains(tuple(-1L, "Wendy"));
  }

  @Test
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/asdf123")
        ).andExpect(status().isNotFound());
  }

  @Test
  public void postReturns201AndHorseValues() throws Exception {
    HorseDetailDto horseDto = new HorseDetailDto(null, "Brandy", "Very Nice", LocalDate.of(2022, 1, 1), Sex.FEMALE, null, null, null);

    MvcResult result = mockMvc.perform(
            post("/horses")
                .content(objectMapper.writeValueAsString(horseDto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value(horseDto.name()))
        .andExpect(jsonPath("$.description").value(horseDto.description()))
        .andReturn();

    Horse horseResult = objectMapper.readerFor(Horse.class).<Horse>
        readValue(result.getResponse().getContentAsByteArray());

    // cleanup:
    horseService.delete(horseResult.getId());
  }

  @Test
  public void deleteReturns204AndThenGetReturns404() throws Exception {
    mockMvc.perform(
            delete("/horses/-10")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$")
            .doesNotExist());
    mockMvc.perform(
            get("/horses/-10")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());

    // cleanup:
    HorseDetailDto toBeCreated = new HorseDetailDto(
        null,
        "Revived Brandy",
        null,
        LocalDate.of(2022, 10, 11),
        Sex.FEMALE,
        null,
        null,
        null);
    mockMvc.perform(
        post("/horses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(toBeCreated))
    );
  }
}
