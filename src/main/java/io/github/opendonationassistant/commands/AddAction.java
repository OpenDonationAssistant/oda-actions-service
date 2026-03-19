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
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AddAction extends BaseController {

  private final ODALogger log = new ODALogger(this);
  private final ActionRepository repository;

  @Inject
  public AddAction(ActionRepository repository) {
    this.repository = repository;
  }

  @Post("/actions/commands/add-actions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @ApiResponse(
    responseCode = "200",
    description = "Actions successfully created",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = AddActionResult.class)
    )
  )
  public CompletableFuture<HttpResponse<List<AddActionResult>>> addAction(
    @Body AddActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  @Post("/actions/commands/addActions")
  @Hidden
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<List<AddActionResult>>> oldAddAction(
    @Body AddActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  private CompletableFuture<HttpResponse<List<AddActionResult>>> execute(
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
