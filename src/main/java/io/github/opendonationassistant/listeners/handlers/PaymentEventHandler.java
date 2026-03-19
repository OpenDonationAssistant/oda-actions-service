package io.github.opendonationassistant.listeners.handlers;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.actions.ActionRequestSender;
import io.github.opendonationassistant.events.actions.ActionRequestSender.ActionRequest;
import io.github.opendonationassistant.events.payments.PaymentEvent;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class PaymentEventHandler extends AbstractMessageHandler<PaymentEvent> {

  private final ActionRepository repository;
  private final ActionRequestSender requestSender;

  @Inject
  public PaymentEventHandler(
    ObjectMapper mapper,
    ActionRepository actionRepository,
    ActionRequestSender requestSender
  ) {
    super(mapper);
    this.repository = actionRepository;
    this.requestSender = requestSender;
  }

  @Override
  public void handle(PaymentEvent payment) throws IOException {
    final List<ActionRequest> actions = payment
      .actions()
      .stream()
      .map(action ->
        repository
          .findByIdAndRecipientId(action.actionId(), payment.recipientId())
          .map(found ->
            new ActionRequestSender.ActionRequest(
              action.id(),
              action.actionId(),
              action.amount(),
              "DonationListener",
              payment.nickname(),
              found.data().payload()
            )
          )
          .orElseThrow(() -> new RuntimeException("Action not found"))
      )
      .toList();
    requestSender.send(payment.recipientId(), actions);
  }
}
