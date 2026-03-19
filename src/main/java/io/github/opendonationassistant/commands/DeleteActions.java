package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.api.DeleteActionsApi;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DeleteActions extends BaseController implements DeleteActionsApi {

  private final ODALogger log = new ODALogger(this);
  private final ActionRepository repository;

  @Inject
  public DeleteActions(ActionRepository repository) {
    this.repository = repository;
  }

  public CompletableFuture<HttpResponse<DeleteActionsResult>> execute(
    DeleteActionsCommand command,
    Authentication auth
  ) {
    Optional<String> ownerIdOpt = getOwnerId(auth);
    if (ownerIdOpt.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    String ownerId = ownerIdOpt.get();
    Optional<List<String>> idsOpt = Optional.ofNullable(command.ids());
    return idsOpt
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
      .thenApply(_ ->
        HttpResponse.ok(
          new DeleteActionsResult(true, "", idsOpt.orElse(List.of()))
        )
      );
  }
}
