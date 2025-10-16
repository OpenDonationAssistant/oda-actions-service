package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.events.config.ConfigCommand;
import io.github.opendonationassistant.events.config.ConfigCommandSender;
import java.util.concurrent.CompletableFuture;

public class Action {

  private final ActionData data;
  private final ActionDataRepository repository;
  private final ConfigCommandSender configCommandSender;

  public Action(
    ActionData data,
    ActionDataRepository repository,
    ConfigCommandSender configCommandSender
  ) {
    this.data = data;
    this.repository = repository;
    this.configCommandSender = configCommandSender;
  }

  public ActionData data() {
    return data;
  }

  public CompletableFuture<Action> save() {
    repository.save(data);
    return configCommandSender
      .send(
        new ConfigCommand.UpsertAction(
          data.recipientId(),
          data.id(),
          data.name(),
          data.amount(),
          data.category(),
          data.game(),
          data.payload()
        )
      )
      .thenApply(d -> this);
  }

  public CompletableFuture<Void> delete() {
    repository.delete(data);
    return configCommandSender.send(
      new ConfigCommand.DeleteAction(data.recipientId(), data.id())
    );
  }
}
