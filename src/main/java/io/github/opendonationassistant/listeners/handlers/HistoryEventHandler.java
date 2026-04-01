package io.github.opendonationassistant.listeners.handlers;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.actions.ActionFacade;
import io.github.opendonationassistant.events.actions.ActionHistoryEvent;
import io.github.opendonationassistant.events.actions.ActionHistoryEvent.ActionRequest;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class HistoryEventHandler
  extends AbstractMessageHandler<HistoryItemEvent> {

  private final ActionRepository repository;
  private final ActionFacade facade;

  public HistoryEventHandler(
    ObjectMapper mapper,
    ActionRepository repository,
    ActionFacade facade
  ) {
    super(mapper);
    this.repository = repository;
    this.facade = facade;
  }

  @Override
  public void handle(HistoryItemEvent message) throws IOException {
    var originId = message.originId();
    if (originId == null) {
      return;
    }
    final List<ActionRequest> actions = message
      .actions()
      .stream()
      .flatMap(action ->
        repository
          .findByActionId(action.actionId())
          .map(found ->
            new ActionHistoryEvent.ActionRequest(
              action.id(),
              action.actionId(),
              action.name(),
              action.amount()
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
