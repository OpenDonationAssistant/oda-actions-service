package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Map;

@Serdeable
@MappedEntity("action")
public record ActionData(
  @Id String id,
  @Nonnull String recipientId,
  @Nullable String category,
  @Nonnull String provider,
  @Nonnull String name,
  @Nonnull Amount amount,
  @Nullable String game,
  @MappedProperty(type = DataType.JSON) @Nonnull Map<String, Object> payload
) {}
