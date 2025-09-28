package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
@MappedEntity("actions")
public record ActionData(
  @Id String id,
  String category,
  String provider,
  String name,
  Amount amount,
  @MappedProperty(type = DataType.JSON) Map<String, Object> payload
) {}
