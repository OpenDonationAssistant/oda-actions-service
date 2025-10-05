package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
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
import java.util.List;
import java.util.Map;

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
  public HttpResponse<List<AddActionResult>> addAction(
    @Body AddActionsCommand command,
    Authentication auth
  ) {
    log.debug("Received AddActionsCommand", Map.of("command", command));
    return getOwnerId(auth)
      .map(ownerId ->
        command
          .actions()
          .stream()
          .map(action ->
            repository.create(
              ownerId,
              action.category(),
              "providerName",
              action.name(),
              action.price(),
              action.game(),
              action.payload()
            )
          )
          .map(Action::save)
          .map(Action::data)
          .map(ActionDto::from)
          .map(dto -> new AddActionResult(true, "", dto))
          .toList()
      )
      .map(HttpResponse::ok)
      .orElseGet(() -> HttpResponse.unauthorized());
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
  public static record NewAction(
    String name,
    String category,
    String game,
    Amount price,
    Map<String, Object> payload
  ) {}
}
