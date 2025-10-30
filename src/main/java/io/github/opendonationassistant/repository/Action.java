package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.events.config.ConfigCommand;
import io.github.opendonationassistant.events.config.ConfigCommandSender;
import java.util.concurrent.CompletableFuture;

public class Action {

  private ActionData data;
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
    return updateConfig().thenApply(v -> this);
  }

  public CompletableFuture<Void> delete() {
    repository.delete(data);
    return deleteConfig();
  }

  public CompletableFuture<Void> enable() {
    this.data = data.withEnabled(true);
    repository.update(data);
    return updateConfig();
  }

  public CompletableFuture<Void> disable() {
    this.data = data.withEnabled(false);
    repository.update(data);
    return deleteConfig();
  }

  private CompletableFuture<Void> deleteConfig() {
    return configCommandSender.send(
      new ConfigCommand.DeleteAction(data.recipientId(), data.id())
    );
  }

  private CompletableFuture<Void> updateConfig() {
    return configCommandSender.send(
      new ConfigCommand.UpsertAction(
        data.recipientId(),
        data.id(),
        data.name(),
        data.amount(),
        data.category(),
        data.game(),
        data.payload()
      )
    );
  }
}
