package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.FamilyTreeQueryParamsDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.stream.Stream;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  static final String BASE_PATH = "/horses";

  private final HorseService service;

  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Stream<HorseListDto> searchHorses(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                           @RequestParam(required = false) Sex sex,
                                           @RequestParam(required = false) Long ownerId,
                                           @RequestParam(required = false) Integer limit,
                                           @RequestParam(required = false) Long idOfHorseToBeExcluded) {
    LOG.info("GET " + BASE_PATH + "?name={}&description={}&dateOfBirth={}&sex={}?ownerId={}&limit={}&idOfHorseToBeExcluded={}", name, description, dateOfBirth,
        sex, ownerId, limit, idOfHorseToBeExcluded);
    HorseSearchDto requestParams = new HorseSearchDto(name, description, dateOfBirth, sex, ownerId, limit, idOfHorseToBeExcluded);
    LOG.debug("request parameters: {}", requestParams);
    try {
      return service.search(requestParams);
    } catch (ValidationException ve) {
      logClientError(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid search RequestParams during horse search", ve);
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ve.getMessage(), ve);
    } catch (ConflictException ce) {
      logClientError(HttpStatus.CONFLICT, "A conflict with the existing state arose while trying to search a horse", ce);
      throw new ResponseStatusException(HttpStatus.CONFLICT, ce.getMessage(), ce);
    }
  }

  @GetMapping("{id}")
  @ResponseStatus(HttpStatus.OK)
  public HorseDetailDto getById(@PathVariable long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HorseDetailDto create(@RequestBody HorseDetailDto createData) throws ValidationException {
    LOG.info("POST " + BASE_PATH);
    LOG.debug("Body of request:\n{}", createData);
    try {
      return service.create(createData);
    } catch (ValidationException ve) {
      logClientError(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid family create horse request body", ve);
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ve.getMessage(), ve);
    } catch (FatalException fe) {
      LOG.error("Error while creating horse.", fe);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, fe.getMessage(), fe);
    } catch (ConflictException ce) {
      logClientError(HttpStatus.CONFLICT, "A conflict with the existing state arose while trying to create a horse", ce);
      throw new ResponseStatusException(HttpStatus.CONFLICT, ce.getMessage(), ce);
    }
  }

  @PutMapping("{id}")
  @ResponseStatus(HttpStatus.OK)
  public HorseDetailDto update(@PathVariable long id, @RequestBody HorseDetailDto updateData) throws ValidationException, ConflictException {
    LOG.info("PUT " + BASE_PATH + "/{}", id);
    LOG.debug("Body of request:\n{}", updateData);
    try {
      return service.update(id, updateData);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException ce) {
      logClientError(HttpStatus.CONFLICT, "A conflict with the existing state arose while trying to update horse with id %d".formatted(id), ce);
      throw new ResponseStatusException(HttpStatus.CONFLICT, ce.getMessage(), ce);
    }
  }

  @DeleteMapping(path = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Long id) {
    LOG.info("DELETE " + BASE_PATH + "/{}", id);
    try {
      service.delete(id);
    } catch (NotFoundException nfe) {
      logClientError(HttpStatus.NOT_FOUND, "Horse to be deleted with id " + id + " doesn't exist", nfe);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Horse to be deleted doesn't exist", nfe);
    } catch (FatalException fe) {
      LOG.error("Error while deleting horse with id {}. Status {}", id, HttpStatus.INTERNAL_SERVER_ERROR.value(), fe);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting horse", fe);
    } catch (ConflictException ce) {
      logClientError(HttpStatus.CONFLICT, "A conflict with the existing state arose while trying to delete horse with id " + id, ce);
      throw new ResponseStatusException(HttpStatus.CONFLICT, ce.getMessage(), ce);
    }
  }

  @GetMapping(path = "/{id}/familyTree")
  @ResponseStatus(HttpStatus.OK)
  public HorseFamilyTreeDto familyTreeOfHorse(@PathVariable Long id, @RequestParam(required = false) Long limit) {
    LOG.info("GET " + BASE_PATH + "/{}/familyTree", id);
    LOG.debug("tree depth limit from request: {}", limit);
    try {
      Long actualLimit = (limit != null) ? limit : 1000;
      LOG.debug("actual tree depth limit: {}", actualLimit);
      return service.getFamilyTree(new FamilyTreeQueryParamsDto(id, actualLimit));
    } catch (ValidationException ve) {
      logClientError(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid family tree request parameters", ve);
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ve.getMessage());
    } catch (NotFoundException nfe) {
      logClientError(HttpStatus.NOT_FOUND, "Horse with id " + id + " not found", nfe);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, nfe.getMessage());
    }
  }

  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}
