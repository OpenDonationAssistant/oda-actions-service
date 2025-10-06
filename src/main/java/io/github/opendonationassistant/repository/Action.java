package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.events.config.ConfigCommand;
import io.github.opendonationassistant.events.config.ConfigCommandSender;

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

  public Action save() {
    repository.save(data);
    try {
      configCommandSender.send(
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public void delete() {
    repository.delete(data);
    try {
      configCommandSender.send(
        new ConfigCommand.DeleteAction(data.recipientId(), data.id())
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
