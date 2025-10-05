package io.github.opendonationassistant.view;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

@Controller
public class ActionController extends BaseController {

  private final ActionRepository repository;

  @Inject
  public ActionController(ActionRepository repository) {
    this.repository = repository;
  }

  @Get
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<List<ActionDto>> getActions(Authentication auth) {
    return getOwnerId(auth)
      .map(ownerId ->
        repository
          .list(ownerId)
          .stream()
          .map(Action::data)
          .map(ActionDto::from)
          .toList()
      )
      .map(HttpResponse::ok)
      .orElseGet(() -> HttpResponse.unauthorized());
  }

  @Serdeable
  public static record ActionDto(
    String id,
    String name,
    Amount price,
    String category,
    String game,
    Map<String, Object> payload
  ) {
    public static ActionDto from(ActionData data) {
      return new ActionDto(
        data.id(),
        data.name(),
        data.amount(),
        data.category(),
        data.game(),
        data.payload()
      );
    }
  }
}
