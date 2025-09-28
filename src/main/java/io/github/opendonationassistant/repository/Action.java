package io.github.opendonationassistant.repository;

public class Action {

  private final ActionData data;
  private final ActionDataRepository repository;

  public Action(ActionData data, ActionDataRepository repository) {
    this.data = data;
    this.repository = repository;
  }

  public ActionData data() {
    return data;
  }
}
