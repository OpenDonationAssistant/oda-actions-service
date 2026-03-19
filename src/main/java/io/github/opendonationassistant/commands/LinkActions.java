package io.github.opendonationassistant.commands;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.ActionRequestData;
import io.github.opendonationassistant.repository.ActionRequestRepository;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LinkActions extends BaseController implements LinkActionsApi {

  private final ODALogger log = new ODALogger(this);
  private final ActionRequestRepository repository;

  @Inject
  public LinkActions(ActionRequestRepository repository) {
    this.repository = repository;
  }

  public CompletableFuture<HttpResponse<LinkActionsResponse>> execute(
    LinkActionsRequest request
  ) {
    return CompletableFuture.runAsync(() ->
      repository.create(
        new ActionRequestData(
          Generators.timeBasedEpochGenerator().generate().toString(),
          "payment",
          request.originId(),
          request
            .actions()
            .stream()
            .map(action ->
              new ActionRequestData.ActionLink(
                Generators.timeBasedEpochGenerator().generate().toString(),
                action.actionId(),
                action.amount()
              )
            )
            .toList()
        )
      )
    ).thenApply(unused -> HttpResponse.ok(new LinkActionsResponse(null)));
  }
}
