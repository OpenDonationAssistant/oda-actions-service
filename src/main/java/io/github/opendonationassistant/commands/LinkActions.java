package io.github.opendonationassistant.commands;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.ActionRequestRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkActions extends BaseController {

  private final ActionRequestRepository repository;

  @Inject
  public LinkActions(ActionRequestRepository repository) {
    this.repository = repository;
  }

  public CompletableFuture<HttpResponse<Void>> linkActions(
    @Body LinkActionsRequest request
  ) {
    return CompletableFuture.runAsync(() ->
      repository.create(
        new io.github.opendonationassistant.repository.ActionRequestData(
          Generators.timeBasedEpochGenerator().generate().toString(),
          "payment",
          request.originId(),
          request
            .actions()
            .stream()
            .map(action ->
              new io.github.opendonationassistant.repository.ActionRequestData.ActionLink(
                Generators.timeBasedEpochGenerator().generate().toString(),
                action.actionId(),
                action.amount()
              )
            )
            .toList()
        )
      )
    ).thenApply(_ -> HttpResponse.ok());
  }

  @Serdeable
  public static record LinkActionsRequest(
    String source,
    String originId,
    List<LinkActions.ActionRequest> actions
  ) {}

  @Serdeable
  public static record ActionRequest(String actionId, Integer amount) {}

  @Serdeable
  public static record LinkActionsResponse(Amount amount) {}
}
