package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.events.actions.ActionRequestSender;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class ActionRequestRepository {

  private final ActionRequestDataRepository repository;
  private final ActionRequestSender sender;

  @Inject
  public ActionRequestRepository(
    ActionRequestDataRepository repository,
    ActionRequestSender sender
  ) {
    this.repository = repository;
    this.sender = sender;
  }

  public List<ActionRequestData> findByOriginId(String originId) {
    return repository.findByOriginId(originId);
  }

  public ActionRequest create(ActionRequestData data) {
    return new ActionRequest(data, sender);
  }
}
