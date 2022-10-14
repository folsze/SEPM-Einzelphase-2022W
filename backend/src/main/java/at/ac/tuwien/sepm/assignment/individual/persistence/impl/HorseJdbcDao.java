package at.ac.tuwien.sepm.assignment.individual.persistence.impl;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
  private static final String SQL_SELECT_MINIMAL_BY_ID = "SELECT horse.id, horse.name, horse.date_of_birth, horse.sex " +
          " FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME +
          " (name, description, date_of_birth, sex, owner_id, mother_id, father_id) " +
          " VALUES (?,?,?,?,?,?,?);";
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
          " SELECT * FROM " + TABLE_NAME + " WHERE " +
          " (? IS NULL OR UPPER(name) LIKE UPPER(?)) AND " +
          " (? IS NULL OR UPPER(description) LIKE UPPER(?)) AND " +
          " (? IS NULL OR date_of_birth < ?) AND " +
          " (? IS NULL OR sex = ?)";
  private static final String SQL_SELECT_ALL_CHILDREN = "SELECT * FROM " + TABLE_NAME + " WHERE mother_id = ? OR father_id = ?";

  private static final String SQL_SEARCH_EXCLUDE_CLAUSE = " AND id != ?";
  private static final String SQL_SEARCH_LIMIT_CLAUSE = " LIMIT ?";



  private final JdbcTemplate jdbcTemplate;

  public HorseJdbcDao(
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Horse> search(HorseSearchDto searchParameters) {
    var args = new ArrayList<>();
    args.add((searchParameters.name() != null) ? '%' + searchParameters.name() + '%' : null );
    args.add((searchParameters.name() != null) ? '%' + searchParameters.name() + '%' : null );
    args.add((searchParameters.description() != null) ? '%' + searchParameters.description() + '%' : null );
    args.add((searchParameters.description() != null) ? '%' + searchParameters.description() + '%' : null );
    args.add(searchParameters.bornBefore()); // todo: convert to sql date?
    args.add(searchParameters.bornBefore()); // todo: convert to sql date?
    args.add(searchParameters.sex() != null ? searchParameters.sex().toString() : null);
    args.add(searchParameters.sex() != null ? searchParameters.sex().toString() : null);

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
    LOG.trace("getAll()");
    try {
      return jdbcTemplate.query(SQL_SELECT_ALL, this::mapRow);
    } catch (DataAccessException dae) {
      throw new FatalException("Error while querying all horses.", dae); // todo Fragestunde: bis in die Persistenz eh ok? Keine Schichteninformation...
    }
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
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
    LOG.trace("getById({})", id);
    if(id == null) return null;

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
    KeyHolder keyHolder = new GeneratedKeyHolder();

    try {
      jdbcTemplate.update(connection -> {
        PreparedStatement stmt = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, toCreate.name());
        stmt.setString(2, toCreate.description());
        stmt.setString(3, java.sql.Date.valueOf(toCreate.dateOfBirth()).toString()); // todo Fragestunde
        stmt.setString(4, toCreate.sex().toString());
        stmt.setObject(5, toCreate.ownerId());
        stmt.setObject(6, toCreate.motherId()); // todo Fragestunde: wird das eine NullPointerException werfen ever? Wie finde ich das in den Docs, dass das sicher nicht so ist?
        stmt.setObject(7, toCreate.fatherId());

        return stmt;
      }, keyHolder);

      return new Horse()
              .setId(((Number)keyHolder.getKeys().get("id")).longValue())
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
            horse.dateOfBirth(),
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
  public void delete(Long id) throws NotFoundException, ConflictException {
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
    } catch (DataIntegrityViolationException dive) {
      throw new ConflictException("Could not delete horse as it is the parent of another horse", List.of("Tried making another horse an orphan"));
    } catch (DataAccessException dae) {
      throw new FatalException("Error when deleting horse", dae);
    }
  }

  @Override
  public List<HorseMinimal> getChildrenOf(Long horseId) {
    try {
      return jdbcTemplate.query(SQL_SELECT_ALL_CHILDREN, this::mapRowMinimal, horseId, horseId);
    } catch (DataAccessException dae) {
      throw new FatalException("Could not get children of mother with id %d".formatted(horseId), dae);
    }
  }


  private Horse mapRow(ResultSet result, int rowNum) throws SQLException {
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDescription(result.getString("description"))
        .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
        .setSex(Sex.valueOf(result.getString("sex")))
        .setOwnerId(result.getObject("owner_id", Long.class))
        .setMotherId(result.getObject("mother_id", Long.class))
        .setFatherId(result.getObject("father_id", Long.class))
        ;
  }

  private HorseMinimal mapRowMinimal(ResultSet result, int rowNum) throws SQLException {
    return new HorseMinimal()
            .setId(result.getLong("id"))
            .setName(result.getString("name"))
            .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
            .setSex(Sex.valueOf(result.getString("sex")))
            ;
  }
}
