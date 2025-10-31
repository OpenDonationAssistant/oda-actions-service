package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
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
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class EnableActions extends BaseController {

  private final ActionRepository repository;

  @Inject
  public EnableActions(ActionRepository repository) {
    this.repository = repository;
  }

  @Post("/actions/commands/enableActions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<EnableActionsResult> enableActions(
    @Body EnableActionsCommand command,
    Authentication auth
  ) {
    return getOwnerId(auth)
      .map(ownerId ->
        getActions(ownerId, command)
          .stream()
          .map(Action::enable)
          .reduce(CompletableFuture.completedFuture(null), (f1, f2) ->
            f1.thenCombine(f2, (a, b) -> null)
          )
      )
      .map(unused -> new EnableActionsResult(true, "", command.ids())) // TODO collect ids
      .map(HttpResponse::ok)
      .orElseGet(() -> HttpResponse.unauthorized());
  }

  private List<Action> getActions(
    String ownerId,
    EnableActionsCommand command
  ) {
    final List<Action> actions = new ArrayList<>();
    if (command.ids() != null) {
      actions.addAll(
        command
          .ids()
          .stream()
          .flatMap(id -> {
            return repository.findByIdAndRecipientId(id, ownerId).stream();
          })
          .toList()
      );
    }
    PredicateSpecification<ActionData> spec = (root, builder) -> {
      final ArrayList<Predicate> conditions = new ArrayList<>();
      if (command.game() != null) {
        conditions.add(builder.equal(root.get("game"), command.game()));
      }
      if (command.category() != null) {
        conditions.add(builder.equal(root.get("category"), command.category()));
      }
      if (command.provider() != null) {
        conditions.add(builder.equal(root.get("provider"), command.provider()));
      }
      return conditions.size() == 0
        ? builder.isTrue(builder.literal(true))
        : builder.and(conditions.toArray(new Predicate[conditions.size()]));
    };
    actions.addAll(repository.findAll(spec));
    return actions;
  }

  @Serdeable
  public static record EnableActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  @Serdeable
  public static record EnableActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable String game,
    @Nullable List<String> ids
  ) {}
}
