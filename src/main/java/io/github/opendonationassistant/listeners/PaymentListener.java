package io.github.opendonationassistant.listeners;

import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.actions.ActionRequestSender;
import io.github.opendonationassistant.events.actions.ActionRequestSender.ActionRequest;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.util.List;

@RabbitListener
public class PaymentListener {

  private final ActionRequestSender requestSender;
  private final ActionRepository repository;

  @Inject
  public PaymentListener(
    ActionRequestSender requestSender,
    ActionRepository repository
  ) {
    this.requestSender = requestSender;
    this.repository = repository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.Payments.ACTIONS)
  void listen(CompletedPaymentNotification payment) {
    if (payment.actions().isEmpty()) {
      return;
    }
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
