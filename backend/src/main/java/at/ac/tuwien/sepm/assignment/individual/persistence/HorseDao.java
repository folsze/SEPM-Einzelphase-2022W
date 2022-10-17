package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.FamilyTreeQueryParamsDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.entity.HorseMinimal;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;

import java.util.List;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {
  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();


  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(Long id, HorseDetailDto horse) throws NotFoundException;

  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Create a horse from a HorseDetailDto in the persistent data store.
   *
   * @param toCreate the data of the horse to be created
   * @return the horse entity that was created
   */
  Horse create(HorseDetailDto toCreate);

  /**
   * Delete a horse by its ID from the persistent data store.
   *
   * @param id the id of the horse to be deleted
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   * @throws ConflictException if the specified horse is already a parent of a horse (not allowed to turn horses into orphans)
   */
  void delete(Long id) throws NotFoundException, ConflictException;

  /**
   * Search all horses by all attributes that a HorseSearchDto has.
   * All search parameters are optional, a search with none specified will return all horses.
   *
   * @param searchParameters a DTO holding all of the search parameters
   * @return the list of Horses that matched the search
   */
  List<Horse> search(HorseSearchDto searchParameters);

  /**
   * gets a minimal form of the normal horse by id.
   * This minimal form doesn't have a description, parents nor an owner.
   *
   * @param id the id of the desired horse
   * @return the desired horse that was searched for with the id
   */
  HorseMinimal getHorseMinimalById(Long id);

  /**
   * Gets all the children of the horse by the horses id
   *
   * @param horseId the id of the horse whose children are desired
   * @return A list of all horses that are children of the horse with the id
   */
  List<HorseMinimal> getChildrenOf(Long horseId);

  /**
   * Gets a list of all horses that are included in the specified horses id
   *
   * @param queryParams the queryParams for the search for the family tree: the horse id and the limit
   * @return A list of all the horses that were queried
   * @throws NotFoundException if the id specified in the queryParams was not found in the database
   */
  List<Horse> getListForFamilyTreeOfHorse(FamilyTreeQueryParamsDto queryParams) throws NotFoundException;
}
