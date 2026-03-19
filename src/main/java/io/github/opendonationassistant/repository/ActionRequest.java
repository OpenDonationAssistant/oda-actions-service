package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.events.actions.ActionRequestSender;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ActionRequest {

  private final ActionRequestData data;
  private final ActionRequestSender requestSender;

  @Inject
  public ActionRequest(
    ActionRequestData data,
    ActionRequestSender requestSender
  ) {
    this.data = data;
    this.requestSender = requestSender;
  }
}
