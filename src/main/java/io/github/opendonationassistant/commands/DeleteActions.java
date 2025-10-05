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
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.List;

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
        command
          .ids()
          .stream()
          .map(id -> {
            repository
              .findByIdAndRecipientId(id, ownerId)
              .ifPresent(Action::delete);
            return id;
          })
          .toList()
      )
      .map(ids -> new DeleteActionsResult(true, "", ids))
      .map(HttpResponse::ok)
      .orElseGet(() -> HttpResponse.unauthorized());
  }

  public static record DeleteActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  public static record DeleteActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable List<String> ids
  ) {}
}
