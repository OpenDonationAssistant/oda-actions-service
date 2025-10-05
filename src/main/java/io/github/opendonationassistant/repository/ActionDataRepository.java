package io.github.opendonationassistant.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ActionDataRepository
  extends CrudRepository<ActionData, String> {
  public List<ActionData> findByRecipientId(String recipientId);
}
