package io.github.opendonationassistant.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ActionRepository {

  private final ActionDataRepository repository;
  private final ODALogger log = new ODALogger(this);

  @Inject
  public ActionRepository(ActionDataRepository repository) {
    this.repository = repository;
  }

  public Optional<Action> findByIdAndRecipientId(
    String id,
    String recipientId
  ) {
    return repository.findById(id).map(data -> new Action(data, repository));
  }

  public List<Action> list(String recipientId) {
    return repository
      .findByRecipientId(recipientId)
      .stream()
      .map(data -> new Action(data, repository))
      .toList();
  }

  public Action create(
    String recipientId,
    String category,
    String provider,
    String name,
    Amount amount,
    String game,
    Map<String, Object> payload
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    var data = new ActionData(
      id,
      recipientId,
      category,
      provider,
      name,
      amount,
      game,
      payload
    );
    log.info("Saving action", Map.of("action", data));
    return new Action(data, repository);
  }
}
