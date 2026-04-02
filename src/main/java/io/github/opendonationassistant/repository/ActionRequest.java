package io.github.opendonationassistant.repository;

import io.github.opendonationassistant.events.actions.ActionRequestSender;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ActionRequest {

  private final ActionRequestData data;
  private final ActionRequestSender requestSender;
  private final ActionRequestDataRepository dataRepository;

  @Inject
  public ActionRequest(
    ActionRequestData data,
    ActionRequestSender requestSender,
    ActionRequestDataRepository dataRepository
  ) {
    this.data = data;
    this.requestSender = requestSender;
    this.dataRepository = dataRepository;
  }

  public void save() {
    dataRepository.save(data);
  }
}
