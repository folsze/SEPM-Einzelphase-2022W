package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.FamilyTreeQueryParamsDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {
  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return he updated horse
   * @throws NotFoundException   if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException   if the update data given for the horse is in conflict the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto update(Long id, HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException;


  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Creates a horse from the data given in the HorseDetailDto parameter
   *
   * @param toCreate a dto holding all the values of the horse that's to be created
   * @return the values of the horse that has been created
   * @throws ValidationException if the create-data is invalid
   * @throws ConflictException   if the create-data would cause a conflict with the existing state of the system
   */
  HorseDetailDto create(HorseDetailDto toCreate) throws ValidationException, ConflictException;

  /**
   * deletes a horse from the system by its id
   *
   * @param id the id of the horse to be deleted
   * @throws NotFoundException if the id provided doesn't map to a horse in the existing state of the program
   */
  void delete(Long id) throws NotFoundException;

  /**
   * searches all horses with parameters specified in a HorseSearchDto
   *
   * @param searchParameters a HorseSearchDto which holds all the parameters that the horse has
   * @return a stream of HorseListDtos that can be used to perform actions with the horses
   * @throws ValidationException if the searchParameters are invalid
   * @throws ConflictException   if the searchParameters conflict with the existing state of the app,
   *                             most of the time this means that an owner was provided which doesn't even exist in the app right now.
   */
  Stream<HorseListDto> search(HorseSearchDto searchParameters) throws ValidationException, ConflictException;

  /**
   * Gets a DTO which holds the family tree of a single horse by the id of the horse and maximum limit of the tree
   *
   * @param queryParams the queryParams of the family tree, which are the id of the horse and the depth-limit of the tree
   * @return a DTO which holds the family tree of a single horse
   * @throws NotFoundException   if the horse from which the family-tree was requested was not found
   * @throws ValidationException if the queryParameters of the request are invalid
   */
  HorseFamilyTreeDto getFamilyTree(FamilyTreeQueryParamsDto queryParams) throws NotFoundException, ValidationException;
}
