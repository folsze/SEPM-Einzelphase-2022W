package at.ac.tuwien.sepm.assignment.individual.persistence.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.FamilyTreeQueryParamsDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.entity.HorseMinimal;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.lang.invoke.MethodHandles;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  private static final String TABLE_NAME = "horse";
  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_MINIMAL_BY_ID = "SELECT horse.id, horse.name, horse.date_of_birth, horse.sex "
      + " FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME
      + " (name, description, date_of_birth, sex, owner_id, mother_id, father_id) "
      + " VALUES (?,?,?,?,?,?,?);";
  private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , description = ?"
      + "  , date_of_birth = ?"
      + "  , sex = ?"
      + "  , owner_id = ?"
      + "  , mother_id = ?"
      + "  , father_id = ?"
      + " WHERE id = ?";
  private static final String SQL_SEARCH =
      " SELECT * FROM " + TABLE_NAME + " WHERE "
          + " (? IS NULL OR UPPER(name) LIKE UPPER(?)) AND "
          + " (? IS NULL OR UPPER(description) LIKE UPPER(?)) AND "
          + " (? IS NULL OR date_of_birth < ?) AND "
          + " (? IS NULL OR sex = ?) AND "
          + " (? IS NULL OR owner_id = ?)";
  private static final String SQL_SELECT_ALL_CHILDREN = "SELECT * FROM " + TABLE_NAME + " WHERE mother_id = ? OR father_id = ?";

  private static final String SQL_SEARCH_EXCLUDE_CLAUSE = " AND id != ?";
  private static final String SQL_SEARCH_LIMIT_CLAUSE = " LIMIT ?";

  private static final String SQL_LIST_FOR_FAMILY_TREE_OF_HORSE = "WITH RECURSIVE pedigree_horse (id, name, date_of_birth, sex, "
      + "mother_id, father_id, generation_number) AS "
      + "(SELECT id, name, date_of_birth, sex, mother_id, father_id, 1 AS generation_number "
      + " FROM horse WHERE id = ?"
      + " UNION "
      + "SELECT horse.id, horse.name, horse.date_of_birth, horse.sex, "
      + " horse.mother_id, horse.father_id, (generation_number + 1) AS generation_number "
      + " FROM horse JOIN pedigree_horse "
      + " ON (horse.id = pedigree_horse.mother_id OR horse.id = pedigree_horse.father_id)"
      + " WHERE generation_number < ?) "
      + "SELECT * FROM pedigree_horse";

  private final JdbcTemplate jdbcTemplate;

  public HorseJdbcDao(
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Horse> search(HorseSearchDto searchParameters) {
    LOG.trace("search horse. params: {}", searchParameters);
    var args = new ArrayList<>();
    args.add((searchParameters.name() != null) ? '%' + searchParameters.name() + '%' : null);
    args.add((searchParameters.name() != null) ? '%' + searchParameters.name() + '%' : null);
    args.add((searchParameters.description() != null) ? '%' + searchParameters.description() + '%' : null);
    args.add((searchParameters.description() != null) ? '%' + searchParameters.description() + '%' : null);
    args.add(searchParameters.bornBefore());
    args.add(searchParameters.bornBefore());
    args.add(searchParameters.sex() != null ? searchParameters.sex().toString() : null);
    args.add(searchParameters.sex() != null ? searchParameters.sex().toString() : null);
    args.add(searchParameters.ownerId());
    args.add(searchParameters.ownerId());

    String query = SQL_SEARCH;
    var excludeThisId = searchParameters.idOfHorseToBeExcluded();
    if (excludeThisId != null) {
      query += SQL_SEARCH_EXCLUDE_CLAUSE;
      args.add(excludeThisId);
    }

    var maxAmount = searchParameters.limit();
    if (maxAmount != null) {
      query += SQL_SEARCH_LIMIT_CLAUSE;
      args.add(maxAmount);
    }

    try {
      return jdbcTemplate.query(query, this::mapRow, args.toArray());
    } catch (DataAccessException dae) {
      throw new FatalException("Error while querying all horses.", dae);
    }
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("horse: getAll()");
    try {
      return jdbcTemplate.query(SQL_SELECT_ALL, this::mapRow);
    } catch (DataAccessException dae) {
      throw new FatalException("Error while querying all horses.", dae);
    }
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("horse: getById({})", id);
    List<Horse> horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public HorseMinimal getHorseMinimalById(Long id) {
    LOG.trace("horse: getMinimalById({})", id);
    if (id == null) {
      return null;
    }

    List<HorseMinimal> horses = jdbcTemplate.query(SQL_SELECT_MINIMAL_BY_ID, this::mapRowMinimal, id);
    if (horses.isEmpty()) {
      throw new FatalException("Horse.motherId != null (motherId=%d) but mother was NotFound. This shouldn't happen.".formatted(id));
    }

    if (horses.size() > 1) {
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public Horse create(HorseDetailDto toCreate) {
    LOG.trace("create: {}", toCreate);
    KeyHolder keyHolder = new GeneratedKeyHolder();

    try {
      jdbcTemplate.update(connection -> {
        PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, toCreate.name());
        stmt.setString(2, toCreate.description());
        stmt.setString(3, Date.valueOf(toCreate.dateOfBirth()).toString());
        stmt.setString(4, toCreate.sex().toString());
        stmt.setObject(5, toCreate.ownerId());
        stmt.setObject(6,
            toCreate.motherId());
        stmt.setObject(7, toCreate.fatherId());

        return stmt;
      }, keyHolder);

      return new Horse()
          .setId(((Number) keyHolder.getKeys().get("id")).longValue())
          .setName(toCreate.name())
          .setDescription(toCreate.description())
          .setDateOfBirth(toCreate.dateOfBirth())
          .setSex(toCreate.sex())
          .setOwnerId(toCreate.ownerId())
          .setMotherId(toCreate.motherId())
          .setFatherId(toCreate.fatherId())
          ;
    } catch (DataAccessException dae) {
      throw new FatalException("Error while adding horse.", dae);
    }
  }

  @Override
  public Horse update(Long id, HorseDetailDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcTemplate.update(SQL_UPDATE,
        horse.name(),
        horse.description(),
        Date.valueOf(horse.dateOfBirth()),
        horse.sex().toString(),
        horse.ownerId(),
        horse.motherId(),
        horse.fatherId(),
        id);
    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }

    return new Horse()
        .setId(id)
        .setName(horse.name())
        .setDescription(horse.description())
        .setDateOfBirth(horse.dateOfBirth())
        .setSex(horse.sex())
        .setOwnerId(horse.ownerId())
        .setMotherId(horse.motherId())
        .setFatherId(horse.fatherId())
        ;
  }

  @Override
  public void delete(Long id) throws NotFoundException {
    LOG.trace("delete horse with id {}", id);

    try {
      int noOfUpdates = jdbcTemplate.update(connection -> {
        PreparedStatement stmt = connection.prepareStatement(SQL_DELETE, Statement.RETURN_GENERATED_KEYS);
        stmt.setLong(1, id);
        return stmt;
      });

      if (noOfUpdates < 1) {
        throw new NotFoundException("Horse to be deleted not found");
      }
    } catch (DataAccessException dae) {
      throw new FatalException("Error when deleting horse", dae);
    }
  }

  @Override
  public List<HorseMinimal> getChildrenOf(Long horseId) {
    LOG.trace("getChildrenOf id={}", horseId);
    try {
      return jdbcTemplate.query(SQL_SELECT_ALL_CHILDREN, this::mapRowMinimal, horseId, horseId);
    } catch (DataAccessException dae) {
      throw new FatalException("Could not get children of mother with id %d".formatted(horseId), dae);
    }
  }

  @Override
  public List<Horse> getListForFamilyTreeOfHorse(FamilyTreeQueryParamsDto queryParams) throws NotFoundException {
    LOG.trace("getListForFamilyTreeOfHorse. Params: {}", queryParams);
    try {
      List<Horse> list = jdbcTemplate.query(SQL_LIST_FOR_FAMILY_TREE_OF_HORSE, this::mapRowFamilyTree, queryParams.horseId(), queryParams.limit());
      if (list.isEmpty()) {
        throw new NotFoundException(String.format("Could not find horse with id %s", queryParams.horseId()));
      }
      return list;
    } catch (DataAccessException e) {
      throw new FatalException("Error when getting list of horses for family tree.", e);
    }
  }

  private Horse mapRow(ResultSet result, int rowNum) throws SQLException {
    LOG.trace("mapRow set:{}, rowNum:{}", result, rowNum);
    Date d1 = result.getDate("date_of_birth");
    LocalDate d = result.getObject("date_of_birth", LocalDate.class);
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDescription(result.getString("description"))
        .setDateOfBirth(result.getObject("date_of_birth", LocalDate.class))
        .setSex(Sex.valueOf(result.getString("sex")))
        .setOwnerId(result.getObject("owner_id", Long.class))
        .setMotherId(result.getObject("mother_id", Long.class))
        .setFatherId(result.getObject("father_id", Long.class))
        ;
  }

  private HorseMinimal mapRowMinimal(ResultSet result, int rowNum) throws SQLException {
    LOG.trace("mapRowMinimal set:{}, rowNum:{}", result, rowNum);
    return new HorseMinimal()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDateOfBirth(result.getObject("date_of_birth", LocalDate.class))
        .setSex(Sex.valueOf(result.getString("sex")))
        ;
  }

  private Horse mapRowFamilyTree(ResultSet result, int rowNum) throws SQLException {
    LOG.trace("mapRowFamilyTree set:{}, rowNum:{}", result, rowNum);
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDateOfBirth(result.getObject("date_of_birth", LocalDate.class))
        .setSex(Sex.valueOf(result.getString("sex")))
        .setMotherId(result.getObject("mother_id", Long.class))
        .setFatherId(result.getObject("father_id", Long.class))
        ;
  }
}
