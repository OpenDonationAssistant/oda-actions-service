package io.github.opendonationassistant.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.actions.ActionSender;
import io.github.opendonationassistant.events.config.ConfigCommandSender;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ActionRepository {

  private final ActionDataRepository repository;
  private final ConfigCommandSender configCommandSender;
  private final ActionSender actionSender;
  private final ODALogger log = new ODALogger(this);

  @Inject
  public ActionRepository(
    ActionDataRepository repository,
    ConfigCommandSender configCommandSender,
    ActionSender actionSender
  ) {
    this.repository = repository;
    this.configCommandSender = configCommandSender;
    this.actionSender = actionSender;
  }

  public Optional<Action> findByIdAndRecipientId(
    String id,
    String recipientId
  ) {
    return repository.findById(id).map(this::convert);
  }

  public List<Action> list(String recipientId) {
    return repository
      .findByRecipientId(recipientId)
      .stream()
      .map(this::convert)
      .toList();
  }

  // TODO should save to db
  public CompletableFuture<Action> create(
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
      true,
      payload
    );
    log.info("Saving action", Map.of("action", data));
    return convert(data)
      .save()
      .thenApply(it -> {
        actionSender.publishCreatedActions(
          List.of(
            new ActionSender.Action(
              it.data().id(),
              // it.data().recipientId(),
              null,
              it.data().category(),
              it.data().provider(),
              it.data().name(),
              it.data().amount(),
              it.data().game(),
              it.data().enabled(),
              it.data().payload()
            )
          )
        );
        return it;
      });
  }

  public List<Action> findAll(PredicateSpecification<ActionData> spec) {
    return convert(repository.findAll(spec));
  }

  private List<Action> convert(List<ActionData> data) {
    return data.stream().map(this::convert).toList();
  }

  private Action convert(ActionData data) {
    return new Action(data, repository, configCommandSender);
  }
}
