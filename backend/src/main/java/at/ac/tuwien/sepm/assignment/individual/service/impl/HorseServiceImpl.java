package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.*;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.entity.HorseMinimal;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public HorseDetailDto update(Long id, HorseDetailDto updateData) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", updateData);
    validator.validateForUpdate(updateData);
    var updatedHorse = dao.update(id, updateData);
    return mapper.entityToDetailDto(
            updatedHorse,
            ownerMapForSingleId(updatedHorse.getOwnerId()),
            horseMapForSingleId(updatedHorse.getMotherId()),
            horseMapForSingleId(updatedHorse.getFatherId())
    );
  }

  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(
        horse,
        ownerMapForSingleId(horse.getOwnerId()),
        horseMapForSingleId(horse.getMotherId()),
        horseMapForSingleId(horse.getFatherId())
    );
  }

  @Override
  public HorseDetailDto create(HorseDetailDto createData) throws ValidationException {
    validator.validateForCreate(createData);
    var createdHorse = dao.create(createData);
    return mapper.entityToDetailDto(
            createdHorse,
            ownerMapForSingleId(createdHorse.getOwnerId()),
            horseMapForSingleId(createdHorse.getMotherId()),
            horseMapForSingleId(createdHorse.getFatherId())
    );
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  private Map<Long, HorseMinimalDto> horseMapForSingleId(Long horseId) {
    return horseId == null
            ? null
            : Collections.singletonMap(horseId, mapper.minimalEntityToMinimalDto(getMinimalHorseById(horseId)));
  }

  private HorseMinimal getMinimalHorseById(Long id) {
    LOG.trace("minimals({})", id);
    return dao.getHorseMinimalById(id);
  }

  @Override
  public void delete(Long id) throws NotFoundException {
    LOG.trace("delete horse with id {}", id);
    dao.delete(id);
  }

  @Override
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) throws ValidationException {
    validator.validateForSearch(searchParameters);
    var horses = dao.search(searchParameters);
    var ownerIds = horses.stream()
            .map(Horse::getOwnerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

    Map<Long, OwnerDto> horsesOwnersMatchingOwnerFullNameSubstring = ownerService.getOwnersByIdsAndFilter(ownerIds,
            new OwnerSearchDto(searchParameters.ownerFullNameSubstring(), null));

//    Stream<Horse> horseStream = horses.stream().filter(horse -> horsesOwnersMatchingOwnerFullNameSubstring.containsKey(horse.getOwnerId()));
    // einkommentieren &: horsesStream.map(... todo Fragestunde
    Stream<HorseListDto> result = horses.stream().map(horse -> mapper.entityToListDto(horse, horsesOwnersMatchingOwnerFullNameSubstring));

    return result;
  }

  @Override
  public List<HorseMinimalDto> getChildrenOf(Long horseId) {
    return dao.getChildrenOf(horseId).stream().map(mapper::minimalEntityToMinimalDto).toList();
  }
}
