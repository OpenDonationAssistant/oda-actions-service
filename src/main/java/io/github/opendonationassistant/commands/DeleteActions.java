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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class DeleteActions extends BaseController {

  private final ActionRepository repository;

  @Inject
  public DeleteActions(ActionRepository repository) {
    this.repository = repository;
  }

  @Post("/actions/commands/deleteActions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<DeleteActionsResult> deleteActions(
    @Body DeleteActionsCommand command,
    Authentication auth
  ) {
    return getOwnerId(auth)
      .map(ownerId ->
        Optional.ofNullable(command.ids())
          .orElse(List.of())
          .stream()
          .map(id -> {
            return repository
              .findByIdAndRecipientId(id, ownerId)
              .map(Action::delete)
              .orElseGet(() -> CompletableFuture.completedFuture(null));
          })
          .reduce(CompletableFuture.completedFuture(null), (f1, f2) ->
            f1.thenCombine(f2, (a, b) -> null)
          )
      )
      .map(unused ->
        new DeleteActionsResult(
          true,
          "",
          Optional.ofNullable(command.ids()).orElse(List.of())
        )
      )
      .map(HttpResponse::ok)
      .orElseGet(() -> HttpResponse.unauthorized());
  }

  @Serdeable
  public static record DeleteActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  @Serdeable
  public static record DeleteActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable List<String> ids
  ) {}
}
