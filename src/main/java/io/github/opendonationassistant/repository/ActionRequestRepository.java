package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.events.actions.ActionRequestSender;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class ActionRequestRepository {

  private final ActionRequestDataRepository repository;
  private final ActionRequestSender sender;
  private final ActionRequestDataRepository dataRepository;

  @Inject
  public ActionRequestRepository(
    ActionRequestDataRepository repository,
    ActionRequestDataRepository dataRepository,
    ActionRequestSender sender
  ) {
    this.repository = repository;
    this.dataRepository = dataRepository;
    this.sender = sender;
  }

  public List<ActionRequestData> findByOriginId(String originId) {
    return repository.findByOriginId(originId);
  }

  public ActionRequest create(ActionRequestData data) {
    return new ActionRequest(data, sender, dataRepository);
  }
}
