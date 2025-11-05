package io.github.opendonationassistant.listeners;

import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.actions.ActionSender;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;

@RabbitListener
public class PaymentListener {

  private final ActionSender actionSender;
  private final ActionRepository repository;

  @Inject
  public PaymentListener(
    ActionSender actionSender,
    ActionRepository repository
  ) {
    this.actionSender = actionSender;
    this.repository = repository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.Payments.ACTIONS)
  void listen(CompletedPaymentNotification payment) {
    if (payment.actions().isEmpty()) {
      return;
    }
    actionSender.send(
      payment.recipientId(),
      payment
        .actions()
        .stream()
        .map(action ->
          repository
            .findByIdAndRecipientId(action.actionId(), payment.recipientId())
            .map(found ->
              new ActionSender.ActionRequest(
                action.id(),
                action.actionId(),
                action.amount(),
                "",
                payment.nickname(),
                found.data().payload()
              )
            )
            .orElseThrow(() -> new RuntimeException("Action not found"))
        )
        .toList()
    );
  }
}
