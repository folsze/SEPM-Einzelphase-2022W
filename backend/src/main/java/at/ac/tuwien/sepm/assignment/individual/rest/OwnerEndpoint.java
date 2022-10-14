package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/owners";

  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }

  @GetMapping
  public Stream<OwnerDto> search(
      @RequestParam(required = false) String fullNameSubstring,
      @RequestParam(required = false) Integer maxResultCount) {
    LOG.info("Post " + BASE_PATH + " query parameters: fullNameSubstring: {}, maxAmount: {}",
        fullNameSubstring, maxResultCount);
    try {
      return service.search(new OwnerSearchDto(fullNameSubstring, maxResultCount));
    } catch (ValidationException ve) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ve.getMessage(), ve);
    }
  }

  @PostMapping
  public OwnerDto create(@RequestBody OwnerCreateDto ownerCreateDto) {
    try {
      return service.create(ownerCreateDto);
    } catch (ValidationException ve) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ve.getMessage(), ve);
    } catch (ConflictException ce) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, ce.getMessage(), ce);
    }
  }
}
