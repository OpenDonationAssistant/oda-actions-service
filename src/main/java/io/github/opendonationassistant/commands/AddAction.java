package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.ActionRepository;
import io.github.opendonationassistant.view.ActionController.ActionDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class AddAction extends BaseController {

  private final ActionRepository repository;
  private final ODALogger log = new ODALogger(this);

  @Inject
  public AddAction(ActionRepository repository) {
    this.repository = repository;
  }

  @Post("/actions/commands/addActions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<List<AddActionResult>>> addAction(
    @Body AddActionsCommand command,
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
        repository.create(
          recipientId.get(),
          action.category(),
          "DonationListener",
          action.name(),
          action.price(),
          action.game(),
          action.payload()
        )
      )
      .map(action ->
        action
          .save()
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

  @Serdeable
  public static record AddActionsCommand(List<NewAction> actions) {}

  @Serdeable
  public static record AddActionResult(
    Boolean success,
    String message,
    ActionDto action
  ) {}

  @Serdeable
  public static record ActionParameter(
    String name,
    String displayName,
    String type
  ) {}

  @Serdeable
  public static record NewAction(
    String name,
    String category,
    String game,
    Amount price,
    Map<String, Object> payload,
    List<ActionParameter> parameters
  ) {}
}
