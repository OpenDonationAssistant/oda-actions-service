package io.github.opendonationassistant.listeners;

import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.actions.ActionSender;
import io.github.opendonationassistant.events.payments.PaymentFacade;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;

@RabbitListener
public class PaymentListener {

  private final ActionSender actionSender;

  @Inject
  public PaymentListener(ActionSender actionSender) {
    this.actionSender = actionSender;
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
          new ActionSender.ActionRequest(
            action.id(),
            action.actionId(),
            "",
            action.payload()
          )
        )
        .toList()
    );
  }
}
