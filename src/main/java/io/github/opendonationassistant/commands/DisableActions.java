package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class DisableActions extends BaseController {

  private final ActionRepository repository;

  @Inject
  public DisableActions(ActionRepository repository) {
    this.repository = repository;
  }

  @Post("/actions/commands/disableActions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<DisableActionsResult> disableActions(
    @Body DisableActionsCommand command,
    Authentication auth
  ) {
    return getOwnerId(auth)
      .map(ownerId ->
        command
          .ids()
          .stream()
          .map(id -> {
            return repository
              .findByIdAndRecipientId(id, ownerId)
              .map(Action::disable)
              .orElseGet(() -> CompletableFuture.completedFuture(null));
          })
          .reduce(CompletableFuture.completedFuture(null), (f1, f2) ->
            f1.thenCombine(f2, (a, b) -> null)
          )
      )
      .map(unused -> new DisableActionsResult(true, "", command.ids())) // TODO collect ids
      .map(HttpResponse::ok)
      .orElseGet(() -> HttpResponse.unauthorized());
  }

  @Serdeable
  public static record DisableActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  @Serdeable
  public static record DisableActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable String game,
    @Nullable List<String> ids
  ) {}
}
