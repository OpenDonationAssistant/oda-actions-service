package io.github.opendonationassistant.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import java.util.List;

@Serdeable
@MappedEntity("action_request")
public record ActionRequestData(
  @Id String id,
  String source,
  @Nullable String originId,
  @MappedProperty(type = DataType.JSON) List<ActionLink> actions
) {
  @Serdeable
  public static record ActionLink(String id, String actionId, Integer amount) {}
}
