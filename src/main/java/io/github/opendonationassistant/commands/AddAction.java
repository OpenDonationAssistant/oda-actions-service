package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionRepository;
import io.github.opendonationassistant.view.ActionController.ActionDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Controller
public class AddAction {

  private final ActionRepository repository;

  @Inject
  public AddAction(ActionRepository repository) {
    this.repository = repository;
  }

  @Post("/actions/commands/addActions")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<List<AddActionResult>> addAction(
    @Body AddActionsCommand command
  ) {
    return HttpResponse.ok(
      command
        .actions()
        .stream()
        .map(action ->
          repository.create(
            action.category(),
            "providerName",
            action.name(),
            action.amount(),
            action.payload()
          )
        )
        .map(Action::data)
        .map(ActionDto::from)
        .map(dto -> new AddActionResult(true, "", dto))
        .toList()
    );
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
    Amount amount,
    Map<String, Object> payload
  ) {}
}
