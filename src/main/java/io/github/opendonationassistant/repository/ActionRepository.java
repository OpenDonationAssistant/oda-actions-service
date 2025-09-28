package io.github.opendonationassistant.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public class ActionRepository {

  private final ActionDataRepository repository;

  @Inject
  public ActionRepository(ActionDataRepository repository) {
    this.repository = repository;
  }

  public Action create(
    String category,
    String provider,
    String name,
    Amount amount,
    Map<String, Object> payload
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    var data = new ActionData(id, category, provider, name, amount, payload);
    return new Action(data, repository);
  }
}
