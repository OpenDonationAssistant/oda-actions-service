package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.ActionRepository;
import io.github.opendonationassistant.view.ActionController.ActionDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AddAction extends BaseController implements AddActionApi {

  private final ODALogger log = new ODALogger(this);
  private final ActionRepository repository;

  @Inject
  public AddAction(ActionRepository repository) {
    this.repository = repository;
  }

  public CompletableFuture<HttpResponse<List<AddActionResult>>> execute(
    AddActionsCommand command,
    Authentication auth
  ) {
    log.debug("Received AddActionsCommand", Map.of("command", command));
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return command
      .actions()
      .stream()
      .map(action ->
        repository
          .create(
            recipientId.get(),
            action.category(),
            "DonationListener",
            action.name(),
            action.price(),
            action.game(),
            action.payload()
          )
          .thenApply(it -> {
            var list = new ArrayList<AddActionResult>();
            list.add(new AddActionResult(true, "", ActionDto.from(it.data())));
            return list;
          })
      )
      .reduce(
        CompletableFuture.completedFuture(new ArrayList<AddActionResult>()),
        (f1, f2) -> {
          return f1.thenCombine(f2, (l1, l2) -> {
            l1.addAll(l2);
            return l1;
          });
        }
      )
      .thenApply(results -> HttpResponse.ok(results));
  }
}
