package io.github.opendonationassistant.listeners.handlers;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.actions.ActionFacade;
import io.github.opendonationassistant.events.actions.ActionHistoryEvent;
import io.github.opendonationassistant.events.actions.ActionHistoryEvent.ActionRequest;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.repository.ActionRepository;
import io.github.opendonationassistant.repository.ActionRequestRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class HistoryEventHandler
  extends AbstractMessageHandler<HistoryItemEvent> {

  private final ActionRepository repository;
  private final ActionFacade facade;
  private final ActionRequestRepository actionRequestRepository;

  public HistoryEventHandler(
    ObjectMapper mapper,
    ActionRepository repository,
    ActionRequestRepository actionRequestRepository,
    ActionFacade facade
  ) {
    super(mapper);
    this.repository = repository;
    this.actionRequestRepository = actionRequestRepository;
    this.facade = facade;
  }

  @Override
  public void handle(HistoryItemEvent message) throws IOException {
    var originId = message.originId();
    if (originId == null) {
      return;
    }
    final List<ActionRequest> actions = actionRequestRepository.findByOriginId(originId)
      .stream()
      .flatMap(request -> request.actions().stream())
      .flatMap(link ->
        repository
          .findByActionId(link.actionId())
          .map(found ->
            new ActionHistoryEvent.ActionRequest(
              link.id(),
              link.actionId(),
              found.data().name(),
              link.amount()
            )
          )
          .stream()
      )
      .toList();
    if (actions.size() > 0) {
      facade.sendEvent(new ActionHistoryEvent("payment", originId, actions));
    }
  }
}
